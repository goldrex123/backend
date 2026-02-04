package sky.spring.pg.infrastructure.pg.toss.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 토스페이먼츠 결제 취소 응답 DTO
 */
@Getter
@NoArgsConstructor
public class TossCancelResponse {

    /**
     * 취소 키
     */
    private String cancelKey;

    /**
     * 취소 금액
     */
    private BigDecimal cancelAmount;

    /**
     * 취소 일시
     */
    private LocalDateTime canceledAt;
}
