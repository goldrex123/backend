package sky.spring.transaction_lock.event_participant.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sky.spring.transaction_lock.event.entity.Event;
import sky.spring.transaction_lock.event.repository.EventRepository;
import sky.spring.transaction_lock.event_participant.entity.EventParticipant;
import sky.spring.transaction_lock.event_participant.entity.Member;
import sky.spring.transaction_lock.event_participant.repository.EventParticipantRepository;
import sky.spring.transaction_lock.event_participant.repository.MemberRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class Event2Service {

    private final EventParticipantRepository eventParticipantRepository;
    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;


    @Transactional
    public void joinEvent(Long eventId, Long memberId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("이벤트를 찾을 수 없습니다"));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다"));

        event.increaseParticipants();
        eventRepository.flush();

        EventParticipant participant = EventParticipant.builder()
                .event(event)
                .member(member)
                .build();

        eventParticipantRepository.save(participant);

    }





}
