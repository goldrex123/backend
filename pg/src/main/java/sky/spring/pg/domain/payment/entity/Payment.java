package sky.spring.pg.domain.payment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sky.spring.pg.common.entity.BaseEntity;
import sky.spring.pg.domain.payment.entity.enums.PaymentMethod;
import sky.spring.pg.domain.payment.entity.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // PG사 결제 키
    @Column(unique = true, length = 255)
    private String paymentKey;

    // 상점 주문 ID
    @Column(unique = true, nullable = false, length = 255)
    private String orderId;

    // 결제 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PaymentStatus status;

    // 결제 수단
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PaymentMethod method;

    // 결제 금액 (BigDecimal 필수)
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    // 고객 정보
    @Column(length = 100)
    private String customerName;

    @Column(length = 100)
    private String customerEmail;

    // 낙관적 락 (동시성 제어)
    @Version
    private Long version;

    // 승인/취소 시각
    private LocalDateTime approvedAt;
    private LocalDateTime canceledAt;

    // 실패 사유
    @Column(columnDefinition = "TEXT")
    private String failReason;

    // 취소 이력 (양방향 관계)
    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentCancel> cancels = new ArrayList<>();

    // 비즈니스 로직 메서드
    public void approve(String paymentKey, LocalDateTime approvedAt) {
        this.paymentKey = paymentKey;
        this.status = PaymentStatus.DONE;
        this.approvedAt = approvedAt;
    }

    public void fail(String failReason) {
        this.status = PaymentStatus.FAILED;
        this.failReason = failReason;
    }

    public void cancel() {
        this.status = PaymentStatus.CANCELED;
        this.canceledAt = LocalDateTime.now();
    }

    public void partialCancel() {
        this.status = PaymentStatus.PARTIAL_CANCELED;
    }

    /**
     * 총 취소 금액 계산
     *
     * 모든 취소 이력의 금액을 합산하여 반환합니다.
     * 취소 이력이 없는 경우 0을 반환합니다.
     *
     * @return 총 취소 금액
     */
    public BigDecimal getTotalCancelAmount() {
        if (cancels.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return cancels.stream()
                .map(PaymentCancel::getCancelAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 취소 가능 금액 계산
     *
     * 원 결제 금액에서 총 취소 금액을 뺀 나머지 금액을 반환합니다.
     *
     * @return 취소 가능 금액
     */
    public BigDecimal getCancelableAmount() {
        return amount.subtract(getTotalCancelAmount());
    }

    /**
     * 취소 이력 추가
     *
     * PaymentCancel을 취소 이력 리스트에 추가합니다.
     * 양방향 관계는 PaymentCancel 생성 시 이미 설정되므로 별도 동기화가 불필요합니다.
     *
     * @param cancel 추가할 취소 이력
     */
    public void addCancel(PaymentCancel cancel) {
        cancels.add(cancel);
    }

    @Builder
    public Payment(String orderId, PaymentStatus status, PaymentMethod method,
                   BigDecimal amount, String customerName, String customerEmail) {
        this.orderId = orderId;
        this.status = status;
        this.method = method;
        this.amount = amount;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
    }
}
