package sky.spring.pg.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;
import sky.spring.pg.domain.payment.entity.Payment;
import sky.spring.pg.domain.payment.entity.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 결제 승인 응답 DTO
 *
 * 결제 승인 완료 후 클라이언트에게 반환할 정보를 담습니다.
 * PG사로부터 받은 결제 키와 승인 시각이 포함됩니다.
 */
@Getter
@Builder
public class PaymentApproveResponse {

  private Long id;
  private String paymentKey;
  private String orderId;
  private PaymentStatus status;
  private BigDecimal amount;
  private LocalDateTime approvedAt;

  /**
   * Payment 엔티티를 PaymentApproveResponse로 변환
   *
   * @param payment 결제 엔티티
   * @return 결제 승인 응답 DTO
   */
  public static PaymentApproveResponse from(Payment payment) {
    return PaymentApproveResponse.builder()
        .id(payment.getId())
        .paymentKey(payment.getPaymentKey())
        .orderId(payment.getOrderId())
        .status(payment.getStatus())
        .amount(payment.getAmount())
        .approvedAt(payment.getApprovedAt())
        .build();
  }
}
