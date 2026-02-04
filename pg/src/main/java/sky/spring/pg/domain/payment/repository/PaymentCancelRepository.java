package sky.spring.pg.domain.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sky.spring.pg.domain.payment.entity.PaymentCancel;

public interface PaymentCancelRepository extends JpaRepository<PaymentCancel, Long> {
}
