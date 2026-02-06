package sky.spring.pg.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sky.spring.pg.domain.payment.entity.enums.PaymentMethod;

import java.math.BigDecimal;

/**
 * 결제 준비 요청 DTO
 *
 * 클라이언트로부터 결제 준비에 필요한 정보를 받습니다.
 * Bean Validation을 통해 필수 필드와 형식을 검증합니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentPrepareRequest {

  @NotBlank(message = "주문 ID는 필수입니다")
  private String orderId;

  @NotNull(message = "결제 금액은 필수입니다")
  @DecimalMin(value = "0", message = "결제 금액은 0 이상이어야 합니다")
  private BigDecimal amount;

  @NotNull(message = "결제 수단은 필수입니다")
  private PaymentMethod method;

  private String customerName;

  @Email(message = "올바른 이메일 형식이 아닙니다")
  private String customerEmail;
}
