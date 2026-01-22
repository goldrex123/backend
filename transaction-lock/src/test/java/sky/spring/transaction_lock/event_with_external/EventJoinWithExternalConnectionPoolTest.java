package sky.spring.transaction_lock.event_with_external;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.shaded.com.google.common.collect.ForwardingIterator;
import sky.spring.transaction_lock.event_participant.entity.Member;
import sky.spring.transaction_lock.event_participant.repository.MemberRepository;
import sky.spring.transaction_lock.event_with_external.facade.EventJoinWithExternalApiFacade;
import sky.spring.transaction_lock.event_with_external.facade.ImprovedEventJoinWithExternalApiFacade;
import sky.spring.transaction_lock.event_with_lock.entity.EventWithLock;
import sky.spring.transaction_lock.event_with_lock.repository.EventWithLockRepository;
import sky.spring.transaction_lock.fixture.ConcurrentTestUtil;
import sky.spring.transaction_lock.fixture.EventFixture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@SpringBootTest
public class EventJoinWithExternalConnectionPoolTest {

    private static final int BACKGROUND_THREAD_COUNT = 28;  // 28개 커넥션 점유
    private static final int TEST_THREAD_COUNT = 20;

    @Autowired
    private EventJoinWithExternalApiFacade originalService;
    @Autowired
    private ImprovedEventJoinWithExternalApiFacade improvedService;
    @Autowired
    private EventWithLockRepository eventRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    private List<EventWithLock> testEvents;
    private List<Member> testMembers;
    private ExecutorService backgroundExecutor;

    @BeforeEach
    void setUp() {
        testEvents = eventRepository.saveAll(EventFixture.createEventWithLocks(TEST_THREAD_COUNT));
        testMembers = memberRepository.saveAll(EventFixture.createMembers(TEST_THREAD_COUNT));
        backgroundExecutor = Executors.newFixedThreadPool(BACKGROUND_THREAD_COUNT);
    }

    @AfterEach
    void cleanUp() {
        backgroundExecutor.shutdownNow();
    }

    @Test
    @DisplayName("커넥션 풀 점유 상황에서 트랜잭션 분리 효과 비교")
    void compareWaitTimeTest() throws InterruptedException {
        occupyConnections();
        Thread.sleep(1000);

        List<Long> originalVersionTimes = Collections.synchronizedList(new ArrayList<>());
        List<Long> improvedVersionTimes = Collections.synchronizedList(new ArrayList<>());

        // when - 원본 버전 테스트
        ConcurrentTestUtil.executeNonConflictingJoins(
                testEvents,
                testMembers,
                (eventId, memberId) -> {
                    long startTime = System.currentTimeMillis();
                    originalService.joinEvent(eventId, memberId);
                    originalVersionTimes.add(System.currentTimeMillis() - startTime);
                }
        );

        // 새로운 이벤트들 생성
        testEvents = eventRepository.saveAll(EventFixture.createEventWithLocks(TEST_THREAD_COUNT));

        // when - 개선된 버전 테스트
        ConcurrentTestUtil.executeNonConflictingJoins(
                testEvents,
                testMembers,
                (eventId, memberId) -> {
                    long startTime = System.currentTimeMillis();
                    improvedService.joinEvent(eventId, memberId);
                    improvedVersionTimes.add(System.currentTimeMillis() - startTime);
                }
        );

        logTestResults("원본 버전", originalVersionTimes);
        logTestResults("개선 버전", improvedVersionTimes);

        double originalAverage = calculateAverage(originalVersionTimes);
        double improvedAverage = calculateAverage(improvedVersionTimes);
        assertThat(improvedAverage).isLessThan(originalAverage * 0.5)
                .as("50%이상 성능 개선!");

    }

    private double calculateAverage(List<Long> times) {
        return times.stream()
                .mapToLong(Long::valueOf)
                .average()
                .orElse(0.0);
    }

    private void logTestResults(String version, List<Long> waitTimes) {
        double averageWaitTime = calculateAverage(waitTimes);
        log.info("=== {} 성능 테스트 결과 ===", version);
        log.info("평균 대기 시간: {}ms", averageWaitTime);
        log.info("최소 대기 시간: {}ms", Collections.min(waitTimes));
        log.info("최대 대기 시간: {}ms", Collections.max(waitTimes));
    }

    private void occupyConnections() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        for (int i = 0; i < BACKGROUND_THREAD_COUNT; i++) {
            backgroundExecutor.submit(() -> {
               transactionTemplate.execute(status -> {
                   try {
                       Thread.sleep(Integer.MAX_VALUE);
                   } catch (InterruptedException e) {
                       Thread.currentThread().interrupt();
                   }
                   return null;
               });
            });
        }
    }

}
