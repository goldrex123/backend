package sky.spring.pg.presentation.controller.webhook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sky.spring.pg.domain.payment.service.PaymentService;
import sky.spring.pg.infrastructure.pg.toss.dto.request.TossWebhookRequest;

/**
 * 토스페이먼츠 웹훅 컨트롤러
 *
 * PG사로부터 비동기로 전달되는 결제 상태 변경 웹훅을 처리합니다.
 * 멱등성을 보장하며, 10초 이내 200 OK 응답을 반환하여 PG사의 재시도를 방지합니다.
 *
 * API 엔드포인트:
 * - POST /api/v1/webhooks/toss: 결제 상태 변경 웹훅
 *
 * 웹훅 헤더:
 * - tosspayments-webhook-transmission-id: 웹훅 고유 식별자
 * - tosspayments-webhook-transmission-time: 웹훅 전송 시각
 * - tosspayments-webhook-transmission-retried-count: 재시도 횟수
 */
@RestController
@RequestMapping("/api/v1/webhooks/toss")
@RequiredArgsConstructor
@Slf4j
public class TossWebhookController {

    private final PaymentService paymentService;

    /**
     * 토스페이먼츠 웹훅 처리
     *
     * 결제 상태 변경 이벤트를 수신하고 처리합니다.
     * PaymentService에 비즈니스 로직을 위임하며, 즉시 200 OK를 응답하여
     * PG사가 재시도하지 않도록 합니다.
     *
     * @param request 웹훅 요청 본문 (eventType, createdAt, data)
     * @param transmissionId 웹훅 고유 식별자 (디버깅 및 추적용)
     * @param transmissionTime 웹훅 전송 시각 (ISO 8601 형식)
     * @param retriedCount 재시도 횟수 (0부터 시작, 기본값 0)
     * @return 200 OK (응답 본문 없음)
     */
    @PostMapping
    public ResponseEntity<Void> handlePaymentWebhook(
        @RequestBody TossWebhookRequest request,
        @RequestHeader("tosspayments-webhook-transmission-id") String transmissionId,
        @RequestHeader("tosspayments-webhook-transmission-time") String transmissionTime,
        @RequestHeader(value = "tosspayments-webhook-transmission-retried-count", defaultValue = "0")
        Integer retriedCount
    ) {
        log.info("웹훅 수신 - id: {}, time: {}, retry: {}, event: {}",
            transmissionId, transmissionTime, retriedCount, request.getEventType());

        // 비즈니스 로직 위임 (트랜잭션 처리는 Service 계층에서)
        paymentService.handlePaymentStatusChanged(request);

        // 빠른 응답 (10초 이내)
        return ResponseEntity.ok().build();
    }
}
