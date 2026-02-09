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
 * 결제 취소 요청 DTO
 *
 * 클라이언트로부터 결제 취소에 필요한 정보를 받습니다.
 * Bean Validation을 통해 필수 필드와 형식을 검증합니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCancelRequest {

  @NotBlank(message = "주문 ID는 필수입니다")
  private String orderId;

  @NotNull(message = "취소 금액은 필수입니다")
  @DecimalMin(value = "0", inclusive = false, message = "취소 금액은 0보다 커야 합니다")
  private BigDecimal cancelAmount;

  @NotBlank(message = "취소 사유는 필수입니다")
  private String cancelReason;
}
