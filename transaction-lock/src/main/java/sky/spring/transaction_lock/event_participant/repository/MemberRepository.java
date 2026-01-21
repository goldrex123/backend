package sky.spring.transaction_lock.event_participant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sky.spring.transaction_lock.event_participant.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
