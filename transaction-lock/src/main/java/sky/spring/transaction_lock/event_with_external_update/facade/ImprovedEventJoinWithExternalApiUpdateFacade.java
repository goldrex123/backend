package sky.spring.transaction_lock.event_with_external_update.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import sky.spring.transaction_lock.event_with_external.external.ExternalEventApi;
import sky.spring.transaction_lock.event_with_external.external.dto.ExternalEventResponse;
import sky.spring.transaction_lock.event_with_external.service.EventExternalUpdateService;
import sky.spring.transaction_lock.event_with_external_update.event.EventJoinCompletedEvent;
import sky.spring.transaction_lock.event_with_lock.entity.EventWithLockParticipant;

@Component
@RequiredArgsConstructor
@Slf4j
public class ImprovedEventJoinWithExternalApiUpdateFacade {

    private static final String TEST_PHONE_NUMBER = "01012341234";

    private final EventExternalUpdateService eventExternalUpdateService;
    private final ExternalEventApi externalEventApi;
    private final ApplicationEventPublisher eventPublisher;

    public void joinEvent(Long eventId, Long memberId) {
        EventWithLockParticipant participant = eventExternalUpdateService.joinEventWithTransaction(eventId, memberId);

        ExternalEventResponse response = externalEventApi.registerParticipant(
                eventId, memberId, participant.getEvent().getName()
        );

        if (!response.isSuccess()) {
            throw new RuntimeException("외부 API 호출 실패: " + response.getErrorMessage());
        }

        // 3. 외부 API 응답으로 참가자 정보 업데이트
        eventExternalUpdateService.updateExternalId(participant, response.getExternalId());

        eventPublisher.publishEvent(new EventJoinCompletedEvent(
                eventId,
                participant.getEvent().getName(),
                TEST_PHONE_NUMBER
        ));
    }

}
