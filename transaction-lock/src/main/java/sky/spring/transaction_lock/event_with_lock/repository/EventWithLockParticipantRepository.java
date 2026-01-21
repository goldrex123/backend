package sky.spring.transaction_lock.event_with_lock.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sky.spring.transaction_lock.event_with_lock.entity.EventWithLockParticipant;

public interface EventWithLockParticipantRepository extends JpaRepository<EventWithLockParticipant,Long> {
}
