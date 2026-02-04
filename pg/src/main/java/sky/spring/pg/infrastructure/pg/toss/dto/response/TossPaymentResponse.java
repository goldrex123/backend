package sky.spring.pg.infrastructure.pg.toss.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 토스페이먼츠 결제 정보 응답 DTO
 */
@Getter
@NoArgsConstructor
public class TossPaymentResponse {

    /**
     * 결제 키
     */
    private String paymentKey;

    /**
     * 주문 ID
     */
    private String orderId;

    /**
     * 결제 상태 (READY, IN_PROGRESS, WAITING_FOR_DEPOSIT, DONE, CANCELED, PARTIAL_CANCELED, ABORTED, EXPIRED)
     */
    private String status;

    /**
     * 총 결제 금액
     */
    private BigDecimal totalAmount;

    /**
     * 결제 수단 (카드, 가상계좌, 간편결제 등)
     */
    private String method;

    /**
     * 결제 승인 일시
     */
    private LocalDateTime approvedAt;
}
