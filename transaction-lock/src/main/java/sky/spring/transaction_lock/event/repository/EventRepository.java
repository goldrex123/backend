package sky.spring.transaction_lock.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sky.spring.transaction_lock.event.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
}
