package sky.spring.pg.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;
import sky.spring.pg.domain.payment.entity.Payment;
import sky.spring.pg.domain.payment.entity.enums.PaymentMethod;
import sky.spring.pg.domain.payment.entity.enums.PaymentStatus;

import java.math.BigDecimal;

/**
 * 결제 준비 응답 DTO
 *
 * 결제 준비 완료 후 클라이언트에게 반환할 정보를 담습니다.
 * 도메인 엔티티를 직접 노출하지 않기 위해 from() 정적 팩토리 메서드를 사용합니다.
 */
@Getter
@Builder
public class PaymentPrepareResponse {

  private Long id;
  private String orderId;
  private BigDecimal amount;
  private PaymentStatus status;
  private PaymentMethod method;

  /**
   * Payment 엔티티를 PaymentPrepareResponse로 변환
   *
   * @param payment 결제 엔티티
   * @return 결제 준비 응답 DTO
   */
  public static PaymentPrepareResponse from(Payment payment) {
    return PaymentPrepareResponse.builder()
        .id(payment.getId())
        .orderId(payment.getOrderId())
        .amount(payment.getAmount())
        .status(payment.getStatus())
        .method(payment.getMethod())
        .build();
  }
}
