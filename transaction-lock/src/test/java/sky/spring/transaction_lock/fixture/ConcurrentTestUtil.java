package sky.spring.transaction_lock.fixture;

import lombok.extern.slf4j.Slf4j;
import sky.spring.transaction_lock.event_participant.entity.Member;
import sky.spring.transaction_lock.event_with_lock.entity.EventWithLock;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ConcurrentTestUtil {

    @FunctionalInterface
    public interface EventJoinTask {
        void join(Long eventId, Long memberId) throws Exception;
    }

    public static void executeConcurrentJoins(
            Long eventId,
            List<Member> members,
            EventJoinTask joinTask
    ) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(members.size());

        for (Member member : members) {
            executorService.submit(() -> {
                try {
                    joinTask.join(eventId, member.getId());
                } catch (Exception e) {
                    log.error("이벤트 참가 실패 - {}", e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdownNow();
    }

    public static void executeNonConflictingJoins(
            List<EventWithLock> events,
            List<Member> members,
            EventJoinTask joinTask
    ) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(events.size());

        for (int i = 0; i < events.size(); i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    joinTask.join(events.get(index).getId(), members.get(index).getId());
                } catch (Exception e) {
                    log.error("이벤트 참가 실패: {}", e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdownNow();
    }
}
