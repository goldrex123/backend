package sky.spring.pg.domain.payment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sky.spring.pg.common.entity.BaseEntity;
import sky.spring.pg.domain.payment.entity.enums.PaymentEventType;

@Entity
@Table(name = "payment_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Payment와 N:1 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    // 이벤트 타입
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PaymentEventType eventType;

    // 요청 본문 (JSON)
    @Column(columnDefinition = "TEXT")
    private String requestBody;

    // 응답 본문 (JSON)
    @Column(columnDefinition = "TEXT")
    private String responseBody;

    // HTTP 상태 코드
    private Integer statusCode;

    @Builder
    public PaymentHistory(Payment payment, PaymentEventType eventType,
                          String requestBody, String responseBody, Integer statusCode) {
        this.payment = payment;
        this.eventType = eventType;
        this.requestBody = requestBody;
        this.responseBody = responseBody;
        this.statusCode = statusCode;
    }
}
