package sky.spring.pg.domain.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sky.spring.pg.domain.payment.entity.PaymentCancel;

import java.util.List;

public interface PaymentCancelRepository extends JpaRepository<PaymentCancel, Long> {

    /**
     * 특정 결제의 모든 취소 이력 조회
     *
     * @param paymentId 결제 ID
     * @return 취소 이력 리스트
     */
    List<PaymentCancel> findByPaymentId(Long paymentId);
}
