package sky.spring.transaction_lock.event_participant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sky.spring.transaction_lock.event_participant.entity.EventParticipant;

public interface EventParticipantRepository extends JpaRepository<EventParticipant, Long> {

    @Query("""
                select  count(ep)
                from EventParticipant ep
                where ep.event.id = :eventId
            """)
    long countByEventId(@Param("eventId") Long eventId);
}
