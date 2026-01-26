package sky.spring.transaction_lock.event_with_external_update;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import sky.spring.transaction_lock.event_participant.entity.Member;
import sky.spring.transaction_lock.event_with_external.external.ExternalEventApi;
import sky.spring.transaction_lock.event_with_external.external.KakaoTalkMessageApi;
import sky.spring.transaction_lock.event_with_external.external.dto.ExternalEventResponse;
import sky.spring.transaction_lock.event_with_external.service.EventExternalUpdateService;
import sky.spring.transaction_lock.event_with_external_update.event.EventJoinCompletedEvent;
import sky.spring.transaction_lock.event_with_external_update.facade.ImprovedEventJoinWithExternalApiUpdateFacade;
import sky.spring.transaction_lock.event_with_lock.entity.EventWithLock;
import sky.spring.transaction_lock.event_with_lock.entity.EventWithLockParticipant;
import sky.spring.transaction_lock.fixture.EventFixture;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@Slf4j
@SpringBootTest
@RecordApplicationEvents
class ImprovedEventJoinWithExternalApiUpdateFacadeTest {

    @Autowired
    private ImprovedEventJoinWithExternalApiUpdateFacade eventJoinFacade;

    @MockitoBean
    private EventExternalUpdateService eventJoinService;

    @MockitoBean
    private ExternalEventApi externalEventApi;

    @MockitoBean
    private KakaoTalkMessageApi kakaoTalkMessageApi;

    @Autowired
    private ApplicationEvents applicationEvents;

    private EventWithLock testEvent;
    private Member testMember;
    private EventWithLockParticipant testParticipant;
    private static final String TEST_PHONE_NUMBER = "01012341234";

    @BeforeEach
    void setUp() {
        testEvent = EventFixture.createEventWithLock("테스트 이벤트", 100);
        testMember = EventFixture.createMember("테스트유저");
        testParticipant = EventFixture.createTestParticipant(testEvent, testMember);

        when(eventJoinService.joinEventWithTransaction(any(), any()))
                .thenReturn(testParticipant);

        String externalId = UUID.randomUUID().toString();
        when(externalEventApi.registerParticipant(any(), any(), any()))
                .thenReturn(ExternalEventResponse.builder()
                        .success(true)
                        .externalId(externalId)
                        .build());
    }

    @Test
    @DisplayName("이벤트 참가 성공 시 이벤트 발행")
    void joinEvent_Success() {
        // when
        eventJoinFacade.joinEvent(testEvent.getId(), testMember.getId());

        // then
        verifyEventPublished();
        verifyBasicMethodCalls();
    }

    @Test
    @DisplayName("이벤트 참가 성공 후 메시지 발송 실패해도 트랜잭션은 커밋됨")
    void joinEvent_SuccessWithMessageFailure() {
        // given
        doThrow(new RuntimeException("메시지 발송 실패"))
                .when(kakaoTalkMessageApi).sendEventJoinMessage(any(), any());

        // when
        eventJoinFacade.joinEvent(testEvent.getId(), testMember.getId());

        // then
        verifyEventPublished();
        verifyBasicMethodCalls();
    }

    //이벤트가 한번만 발행되었는지 검증
    private void verifyEventPublished() {
        assertThat(applicationEvents.stream(EventJoinCompletedEvent.class).count())
                .isEqualTo(1);

        EventJoinCompletedEvent event = applicationEvents.stream(EventJoinCompletedEvent.class)
                .findFirst()
                .orElseThrow();

        assertThat(event)
                .satisfies(e -> {
                    assertThat(e.getEventId()).isEqualTo(testEvent.getId());
                    assertThat(e.getEventName()).isEqualTo(testEvent.getName());
                    assertThat(e.getPhoneNumber()).isEqualTo("01012341234");
                });
    }

    private void verifyBasicMethodCalls() {
        verify(eventJoinService, times(1))
                .joinEventWithTransaction(eq(testEvent.getId()), eq(testMember.getId()));
        verify(externalEventApi, times(1))
                .registerParticipant(eq(testEvent.getId()), eq(testMember.getId()), eq(testEvent.getName()));
        verify(eventJoinService, times(1))
                .updateExternalId(eq(testParticipant), any());
    }
}