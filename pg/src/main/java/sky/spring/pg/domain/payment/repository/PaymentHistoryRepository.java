package sky.spring.pg.domain.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sky.spring.pg.domain.payment.entity.PaymentHistory;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {
}
