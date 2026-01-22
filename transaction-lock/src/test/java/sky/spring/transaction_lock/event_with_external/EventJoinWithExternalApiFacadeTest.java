package sky.spring.transaction_lock.event_with_external;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

@Slf4j
@SpringBootTest
public class EventJoinWithExternalApiFacadeTest {

    private static final int TEST_THREAD_COUNT = 20;

    @Autowired
    private EventJoinWithExternalApiFacade originalService;
    @Autowired
    private ImprovedEventJoinWithExternalApiFacade improvedService;
    @Autowired
    private EventWithLockRepository eventRepository;
    @Autowired
    private MemberRepository memberRepository;

    private List<EventWithLock> testEvents;
    private List<Member> testMembers;

    @BeforeEach
    void setUp() {
        testEvents = eventRepository.saveAll(EventFixture.createEventWithLocks(TEST_THREAD_COUNT));
        testMembers = memberRepository.saveAll(EventFixture.createMembers(TEST_THREAD_COUNT));
    }

    @Test
    @DisplayName("동시 요청이 커넥션 풀 사이즈보다 적은 경우, 트랜잭션 범위가 길어도 성능 차이 X")
    void comparePerformanceTest() throws InterruptedException {
        List<Long> originalVersionTimes = Collections.synchronizedList(new ArrayList<>());
        List<Long> improvedVersionTimes = Collections.synchronizedList(new ArrayList<>());

        long originalStartTime = System.currentTimeMillis();
        ConcurrentTestUtil.executeNonConflictingJoins(testEvents, testMembers,
                (eventId, memberId) -> {
                    long startTime = System.currentTimeMillis();
                    originalService.joinEvent(eventId, memberId);
                    originalVersionTimes.add(System.currentTimeMillis() - startTime);
                }
        );

        testEvents = eventRepository.saveAll(EventFixture.createEventWithLocks(TEST_THREAD_COUNT));

        long improvedStartTime = System.currentTimeMillis();
        ConcurrentTestUtil.executeNonConflictingJoins(testEvents, testMembers,
                (eventId, memberId) -> {
                    long startTime = System.currentTimeMillis();
                    improvedService.joinEvent(eventId,memberId);
                    improvedVersionTimes.add(System.currentTimeMillis() - startTime);
                }
        );

        logTestResults("원본 버전(긴 트랜잭션)",originalStartTime, originalVersionTimes);
        logTestResults("개선 버전(짧은 트랜잭션)",improvedStartTime, improvedVersionTimes);
    }

    private double calculateAverage(List<Long> times) {
        return times.stream()
                .mapToLong(Long::valueOf)
                .average()
                .orElse(0.0);
    }

    private void logTestResults(String version, long startTime, List<Long> executionTimes) {
        long totalExecutionTime = System.currentTimeMillis() - startTime;
        double averageExecutionTime = calculateAverage(executionTimes);

        log.info("=== {} 성능 테스트 결과 ===", version);
        log.info("동시 요청 수: {}", testMembers.size());
        log.info("커넥션 풀 사이즈: 32");
        log.info("총 실행 시간: {}ms", totalExecutionTime);
        log.info("평균 실행 시간: {}ms", averageExecutionTime);
        log.info("최소 실행 시간: {}ms", Collections.min(executionTimes));
        log.info("최대 실행 시간: {}ms", Collections.max(executionTimes));
    }
}
