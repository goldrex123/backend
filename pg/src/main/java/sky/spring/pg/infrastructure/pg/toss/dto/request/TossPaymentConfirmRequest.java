package sky.spring.pg.infrastructure.pg.toss.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 토스페이먼츠 결제 승인 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TossPaymentConfirmRequest {

    /**
     * 결제 키 (토스페이먼츠에서 발급)
     */
    private String paymentKey;

    /**
     * 주문 ID (가맹점에서 생성)
     */
    private String orderId;

    /**
     * 결제 금액
     */
    private BigDecimal amount;
}
