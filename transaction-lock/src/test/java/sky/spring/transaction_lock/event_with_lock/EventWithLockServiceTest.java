package sky.spring.transaction_lock.event_with_lock;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sky.spring.transaction_lock.event_participant.entity.Member;
import sky.spring.transaction_lock.event_participant.repository.MemberRepository;
import sky.spring.transaction_lock.event_with_lock.entity.EventWithLock;
import sky.spring.transaction_lock.event_with_lock.facade.NamedLockEventFacade;
import sky.spring.transaction_lock.event_with_lock.facade.OptimisticLockEventFacade;
import sky.spring.transaction_lock.event_with_lock.repository.EventWithLockParticipantRepository;
import sky.spring.transaction_lock.event_with_lock.repository.EventWithLockRepository;
import sky.spring.transaction_lock.event_with_lock.service.EventWithLockService;
import sky.spring.transaction_lock.fixture.ConcurrentTestUtil;
import sky.spring.transaction_lock.fixture.EventFixture;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Slf4j
public class EventWithLockServiceTest {

    @Autowired
    private OptimisticLockEventFacade optimisticLockEventFacade;
    @Autowired
    private NamedLockEventFacade namedLockEventFacade;
    @Autowired
    private EventWithLockService eventWithLockService;
    @Autowired
    private EventWithLockRepository eventRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private EventWithLockParticipantRepository participantRepository;

    private EventWithLock testEvent;
    private List<Member> testMembers;

    private static final int THREAD_COUNT = 100;

    @BeforeEach
    void setUp() {
        testEvent = eventRepository.save(
                EventFixture.createEventWithLock("테스트 이벤트", 100)
        );

        testMembers = memberRepository.saveAll(
                EventFixture.createMembers(THREAD_COUNT)
        );
    }

    @AfterEach
    void cleanUp() {
        participantRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("비관적 락으로 100명 동시 참가 테스트")
    void pessimisticLockTest() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        ConcurrentTestUtil.executeConcurrentJoins(
                testEvent.getId(),
                testMembers,
                (eventId, memberId) -> eventWithLockService.joinEventPessimistic(eventId, memberId)
        );
        long executeTime = System.currentTimeMillis() - startTime;

        EventWithLock updatedEvent = eventRepository.findById(testEvent.getId()).orElseThrow();

        log.info("=== 비관적 락 테스트 결과 ===");
        log.info("실행 시간: {}ms", executeTime); // 666
        log.info("최종 참가자 수: {}", updatedEvent.getCurrentParticipants());

        assertThat(updatedEvent.getCurrentParticipants()).isEqualTo(THREAD_COUNT);
    }

    @Test
    @DisplayName("낙관적 락으로 100명 동시 참가 테스트")
    void optimisticLockTest() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        ConcurrentTestUtil.executeConcurrentJoins(
                testEvent.getId(),
                testMembers,
                (eventId, memberId) -> optimisticLockEventFacade.joinEvent(eventId, memberId)
        );
        long executeTime = System.currentTimeMillis() - startTime;

        EventWithLock updatedEvent = eventRepository.findById(testEvent.getId()).orElseThrow();

        log.info("=== 낙관적 락 테스트 결과 ===");
        log.info("실행 시간: {}ms", executeTime); // 3521
        log.info("최종 참가자 수: {}", updatedEvent.getCurrentParticipants());

        assertThat(updatedEvent.getCurrentParticipants()).isEqualTo(THREAD_COUNT);

    }

    @Test
    @DisplayName("네임드 락으로 100명 동시 참가 테스트")
    void namedLockTest() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        ConcurrentTestUtil.executeConcurrentJoins(
                testEvent.getId(),
                testMembers,
                (eventId, memberId) -> namedLockEventFacade.joinEvent(eventId, memberId)
        );
        long executionTime = System.currentTimeMillis() - startTime;

        EventWithLock updatedEvent = eventRepository.findById(testEvent.getId()).orElseThrow();

        log.info("=== 네임드 락 테스트 결과 ===");
        log.info("실행 시간: {}ms", executionTime);
        log.info("최종 참가자 수: {}", updatedEvent.getCurrentParticipants());

        assertThat(updatedEvent.getCurrentParticipants()).isEqualTo(THREAD_COUNT);
    }
}
