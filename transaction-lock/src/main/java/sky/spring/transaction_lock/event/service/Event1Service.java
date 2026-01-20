package sky.spring.transaction_lock.event.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sky.spring.transaction_lock.event.entity.Event;
import sky.spring.transaction_lock.event.repository.EventRepository;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class Event1Service {

    private final EventRepository eventRepository;

    public void increaseParticipant(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("이벤트를 찾을 수 없습니다"));

        event.increaseParticipants();
    }
}
