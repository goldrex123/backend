package sky.spring.pg.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;
import sky.spring.pg.domain.payment.entity.Payment;
import sky.spring.pg.domain.payment.entity.PaymentCancel;
import sky.spring.pg.domain.payment.entity.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 결제 취소 응답 DTO
 *
 * 결제 취소 완료 후 클라이언트에게 반환할 정보를 담습니다.
 * 도메인 엔티티를 직접 노출하지 않기 위해 from() 정적 팩토리 메서드를 사용합니다.
 */
@Getter
@Builder
public class PaymentCancelResponse {

  private Long paymentId;
  private String orderId;
  private PaymentStatus status;  // CANCELED 또는 PARTIAL_CANCELED
  private BigDecimal totalAmount;  // 원 결제 금액
  private BigDecimal totalCancelAmount;  // 총 취소 금액
  private BigDecimal cancelableAmount;  // 남은 취소 가능 금액
  private BigDecimal currentCancelAmount;  // 이번 취소 금액
  private String cancelReason;  // 이번 취소 사유
  private LocalDateTime canceledAt;  // 이번 취소 시각

  /**
   * Payment 엔티티와 PaymentCancel 엔티티를 PaymentCancelResponse로 변환
   *
   * @param payment 결제 엔티티
   * @param cancel 취소 이력 엔티티
   * @return 결제 취소 응답 DTO
   */
  public static PaymentCancelResponse from(Payment payment, PaymentCancel cancel) {
    return PaymentCancelResponse.builder()
        .paymentId(payment.getId())
        .orderId(payment.getOrderId())
        .status(payment.getStatus())
        .totalAmount(payment.getAmount())
        .totalCancelAmount(payment.getTotalCancelAmount())
        .cancelableAmount(payment.getCancelableAmount())
        .currentCancelAmount(cancel.getCancelAmount())
        .cancelReason(cancel.getCancelReason())
        .canceledAt(cancel.getCanceledAt())
        .build();
  }
}
