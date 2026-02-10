package sky.spring.pg.infrastructure.pg.toss.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 토스페이먼츠 웹훅 요청 DTO
 *
 * PG사로부터 비동기로 전달되는 결제 상태 변경 웹훅 페이로드를 매핑합니다.
 * 웹훅 이벤트 타입과 결제 데이터를 포함하며, JSON 역직렬화를 통해 자동으로 매핑됩니다.
 */
@Getter
@NoArgsConstructor
public class TossWebhookRequest {

    /**
     * 웹훅 이벤트 타입
     *
     * 예: PAYMENT_STATUS_CHANGED (결제 상태 변경)
     */
    private String eventType;

    /**
     * 웹훅 발생 시각
     *
     * ISO 8601 형식의 타임스탬프 문자열
     */
    private String createdAt;

    /**
     * 결제 데이터
     *
     * 웹훅 이벤트와 관련된 결제 정보를 포함합니다.
     */
    private PaymentData data;

    /**
     * 결제 데이터 내부 클래스
     *
     * 웹훅으로 전달되는 결제의 상세 정보를 담고 있습니다.
     * 결제 키, 주문 ID, 상태, 금액 등 결제 처리에 필요한 모든 정보를 포함합니다.
     */
    @Getter
    @NoArgsConstructor
    public static class PaymentData {

        /**
         * 결제 키
         *
         * 토스페이먼츠에서 발급한 결제 고유 식별자
         */
        private String paymentKey;

        /**
         * 주문 ID
         *
         * 가맹점에서 생성한 주문 고유 식별자
         */
        private String orderId;

        /**
         * 결제 상태
         *
         * DONE (승인 완료), CANCELED (취소됨), 그 외 토스페이먼츠 결제 상태
         */
        private String status;

        /**
         * 총 결제 금액
         *
         * 부동소수점 오차 방지를 위해 BigDecimal 사용
         */
        private BigDecimal totalAmount;

        /**
         * 결제 수단
         *
         * 카드, 가상계좌, 간편결제 등의 결제 방법
         */
        private String method;

        /**
         * 결제 승인 일시
         *
         * 결제가 승인된 시각 (승인된 경우에만 존재)
         */
        private LocalDateTime approvedAt;
    }
}
