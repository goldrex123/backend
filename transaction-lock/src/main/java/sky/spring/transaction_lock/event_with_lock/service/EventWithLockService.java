package sky.spring.transaction_lock.event_with_lock.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import sky.spring.transaction_lock.event_participant.entity.Member;
import sky.spring.transaction_lock.event_participant.repository.MemberRepository;
import sky.spring.transaction_lock.event_with_lock.entity.EventWithLock;
import sky.spring.transaction_lock.event_with_lock.entity.EventWithLockParticipant;
import sky.spring.transaction_lock.event_with_lock.repository.EventWithLockParticipantRepository;
import sky.spring.transaction_lock.event_with_lock.repository.EventWithLockRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventWithLockService {

    private final EventWithLockRepository eventWithLockRepository;
    private final EventWithLockParticipantRepository eventWithLockParticipantRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void joinEventPessimistic(Long eventId, Long memberId) {
        EventWithLock event = eventWithLockRepository.findByIdWithPessimisticLock(eventId)
                .orElseThrow(() -> new EntityNotFoundException("이벤트를 찾을 수 없습니다."));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        event.increaseParticipants();

        EventWithLockParticipant participant = EventWithLockParticipant.builder()
                .event(event)
                .member(member)
                .build();

        eventWithLockParticipantRepository.save(participant);
    }

    @Transactional
    public void joinEventOptimistic(Long eventId, Long memberId) {
        EventWithLock event = eventWithLockRepository.findByIdOptimisticLock(eventId)
                .orElseThrow(() -> new EntityNotFoundException("이벤트를 찾을 수 없습니다"));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        event.increaseParticipants();
        eventWithLockRepository.flush();

        EventWithLockParticipant participant = EventWithLockParticipant.builder()
                .event(event)
                .member(member)
                .build();

        eventWithLockParticipantRepository.save(participant);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void joinEventWithNamedLock(Long eventId, Long memberId) {
        EventWithLock event = eventWithLockRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("이벤트를 찾을 수 없습니다."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        event.increaseParticipants();
        eventWithLockRepository.flush();

        EventWithLockParticipant participant = EventWithLockParticipant.builder()
                .event(event)
                .member(member)
                .build();
        eventWithLockParticipantRepository.save(participant);
    }
}
