package sky.spring.transaction_lock.event_participant;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sky.spring.transaction_lock.event.entity.Event;
import sky.spring.transaction_lock.event.repository.EventRepository;
import sky.spring.transaction_lock.event_participant.entity.EventParticipant;
import sky.spring.transaction_lock.event_participant.entity.Member;
import sky.spring.transaction_lock.event_participant.repository.EventParticipantRepository;
import sky.spring.transaction_lock.event_participant.repository.MemberRepository;
import sky.spring.transaction_lock.event_participant.service.Event2Service;
import sky.spring.transaction_lock.fixture.ConcurrentTestUtil;
import sky.spring.transaction_lock.fixture.EventFixture;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@SpringBootTest
public class Event2ServiceTest {

    @Autowired
    private Event2Service event2Service;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EventParticipantRepository eventParticipantRepository;


    private Event testEvent;

    private List<Member> testMembers;

    @BeforeEach
    void setUp() {
        testEvent = eventRepository.save(
                EventFixture.createEvent("테스트 이벤트", 100)
        );

        testMembers = memberRepository.saveAll(
                EventFixture.createMembers(150)
        );
    }

    @Test
    @DisplayName("트랜잭션만으로는 동시성 제어가 되지 않는다")
    void transactionDoesNotGuranteeAtomicity() throws InterruptedException {
        ConcurrentTestUtil.executeConcurrentJoins(
                testEvent.getId(),
                testMembers,
                (eventId, memberId) -> event2Service.joinEvent(eventId, memberId)
        );

        Event updatedEvent = eventRepository.findById(testEvent.getId()).orElseThrow();
        long actualParticipantCount = eventParticipantRepository.countByEventId(testEvent.getId());

        log.info("=== 트랜잭션 동시성 테스트 결과 ===");
        log.info("동등 여부: {}", testEvent == updatedEvent);
        log.info("이벤트 최대 참가 인원: {}", testEvent.getMaxParticipants());
        log.info("이벤트 현재 참가자 수: {}", updatedEvent.getCurrentParticipants());
        log.info("실제 참가자 테이블 레코드 수: {}", actualParticipantCount);

        assertThat(updatedEvent.getCurrentParticipants()).isNotEqualTo(actualParticipantCount);
        assertThat(updatedEvent.getCurrentParticipants()).isLessThanOrEqualTo(testEvent.getMaxParticipants());
    }
}
