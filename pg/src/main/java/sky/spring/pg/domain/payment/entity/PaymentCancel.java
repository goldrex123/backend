package sky.spring.pg.domain.payment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sky.spring.pg.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_cancels")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentCancel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Payment와 N:1 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    // 취소 금액
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal cancelAmount;

    // 취소 사유
    @Column(nullable = false, length = 500)
    private String cancelReason;

    // 취소 시각
    private LocalDateTime canceledAt;

    @Builder
    public PaymentCancel(Payment payment, BigDecimal cancelAmount,
                         String cancelReason, LocalDateTime canceledAt) {
        this.payment = payment;
        this.cancelAmount = cancelAmount;
        this.cancelReason = cancelReason;
        this.canceledAt = canceledAt;
    }
}
