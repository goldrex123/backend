package sky.spring.pg.infrastructure.pg.toss.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 토스페이먼츠 결제 취소 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TossCancelRequest {

    /**
     * 취소 사유
     */
    private String cancelReason;
}
