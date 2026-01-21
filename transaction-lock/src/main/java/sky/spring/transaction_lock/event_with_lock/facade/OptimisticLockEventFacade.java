package sky.spring.transaction_lock.event_with_lock.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.stereotype.Service;
import sky.spring.transaction_lock.event_with_lock.service.EventWithLockService;

@Service
@RequiredArgsConstructor
@Slf4j
public class OptimisticLockEventFacade {
    private final EventWithLockService eventWithLockService;

    //지연 시간
    private static final long RETRY_DELAY_MS = 50;

    public void joinEvent(Long eventId, Long memberId) throws InterruptedException {
        int retryCount = 0;

        while (true) {
            try {
                eventWithLockService.joinEventOptimistic(eventId,memberId);
                log.info("이벤트 참가 성공 - eventId: {}, memberId: {}, 총 시도 횟수: {}",
                        eventId, memberId, retryCount+1);

                return ;
            } catch (Exception e) {
                retryCount++;

                log.warn("이벤트 참가 재시도 - eventId: {}, memberId: {}, 현재 시도 횟수: {}, error: {}",
                        eventId, memberId, retryCount, e.getMessage());
                Thread.sleep(RETRY_DELAY_MS);
            }
        }
    }

}
