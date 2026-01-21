package sky.spring.transaction_lock.event_with_lock.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import sky.spring.transaction_lock.event_with_lock.entity.EventWithLock;

import java.util.Optional;

public interface EventWithLockRepository extends JpaRepository<EventWithLock, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select e
            from EventWithLock e
            where e.id = :id
            """)
    Optional<EventWithLock> findByIdWithPessimisticLock(Long id);
}

