package sky.spring.pg.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 결제 승인 요청 DTO
 *
 * PG사로부터 받은 결제 키와 함께 최종 승인을 요청합니다.
 * 금액 검증을 통해 결제 위변조를 방지합니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentApproveRequest {

  @NotBlank(message = "결제 키는 필수입니다")
  private String paymentKey;

  @NotBlank(message = "주문 ID는 필수입니다")
  private String orderId;

  @NotNull(message = "결제 금액은 필수입니다")
  @DecimalMin(value = "0", message = "결제 금액은 0 이상이어야 합니다")
  private BigDecimal amount;
}
