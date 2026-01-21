package sky.spring.transaction_lock.event_with_lock.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sky.spring.transaction_lock.event_with_lock.entity.EventWithLock;

import java.util.Optional;

public interface EventWithLockRepository extends JpaRepository<EventWithLock, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE) // 비관적락 x 락 획득
    @Query("""
            select e
            from EventWithLock e
            where e.id = :id
            """)
    Optional<EventWithLock> findByIdWithPessimisticLock(Long id);

    @Lock(LockModeType.OPTIMISTIC) // 낙관적 락
    @Query("""
            select e
            from EventWithLock e
            where e.id = :id
            """
    )
    Optional<EventWithLock> findByIdOptimisticLock(Long id);

    @Query(value = "SELECT GET_LOCK(:lockName, :timeoutSeconds)", nativeQuery = true)
    Integer getLock(@Param("lockName") String lockName, @Param("timeoutSeconds") int timeoutSeconds);

    @Query(value = "SELECT RELEASE_LOCK(:lockName)", nativeQuery = true)
    Integer releaseLock(@Param("lockName") String lockName);
}

