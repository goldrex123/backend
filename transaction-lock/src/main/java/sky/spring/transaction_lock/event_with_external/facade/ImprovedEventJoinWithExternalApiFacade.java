package sky.spring.transaction_lock.event_with_external.facade;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sky.spring.transaction_lock.event_with_external.external.ExternalEventApi;
import sky.spring.transaction_lock.event_with_external.external.KakaoTalkMessageApi;
import sky.spring.transaction_lock.event_with_external.external.dto.ExternalEventResponse;
import sky.spring.transaction_lock.event_with_external.service.EventExternalUpdateService;
import sky.spring.transaction_lock.event_with_lock.entity.EventWithLock;
import sky.spring.transaction_lock.event_with_lock.repository.EventWithLockRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class ImprovedEventJoinWithExternalApiFacade {

    private static final String TEST_PHONE_NUMBER = "01012341234";

    private final EventExternalUpdateService eventExternalUpdateService;
    private final ExternalEventApi externalEventApi;
    private final KakaoTalkMessageApi kakaoTalkMessageApi;
    private final EventWithLockRepository eventWithLockRepository;

    public void joinEvent(Long eventId, Long memberId) {
        EventWithLock event = eventWithLockRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("이벤트를 찾을 수 없습니다"));

        eventExternalUpdateService.joinEventWithTransaction(eventId, memberId);

        ExternalEventResponse response = externalEventApi.registerParticipant(eventId, memberId, event.getName());

        if (!response.isSuccess()) {
            log.error("외부 API 호출 실패. 이벤트: {}, 회원: {}", eventId, memberId);
        }

        try {
            kakaoTalkMessageApi.sendEventJoinMessage(TEST_PHONE_NUMBER, event.getName());
        } catch (Exception e) {
            log.error("알림 발송 실패", e);
        }
    }
}
