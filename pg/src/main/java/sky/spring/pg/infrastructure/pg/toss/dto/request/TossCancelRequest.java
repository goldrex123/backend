package sky.spring.pg.infrastructure.pg.toss.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 토스페이먼츠 결제 취소 요청 DTO
 *
 * 부분 취소 지원:
 * - cancelAmount가 null이면 전액 취소
 * - cancelAmount가 있으면 해당 금액만큼 부분 취소
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TossCancelRequest {

    /**
     * 취소 금액
     * null이면 전액 취소, 값이 있으면 부분 취소
     */
    private BigDecimal cancelAmount;

    /**
     * 취소 사유
     */
    private String cancelReason;
}
