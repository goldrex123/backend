package sky.spring.pg.application;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import sky.spring.pg.domain.payment.entity.Payment;
import sky.spring.pg.domain.payment.entity.PaymentHistory;
import sky.spring.pg.domain.payment.entity.enums.PaymentEventType;
import sky.spring.pg.domain.payment.entity.enums.PaymentMethod;
import sky.spring.pg.domain.payment.entity.enums.PaymentStatus;
import sky.spring.pg.domain.payment.repository.PaymentHistoryRepository;
import sky.spring.pg.domain.payment.repository.PaymentRepository;
import sky.spring.pg.infrastructure.pg.toss.dto.request.TossWebhookRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 웹훅 처리 통합 테스트
 *
 * 웹훅 엔드포인트부터 DB 저장까지 전체 플로우를 검증합니다.
 * 정상 시나리오, 멱등성, 예외 상황을 테스트합니다.
 */
@SpringBootTest
@ActiveProfiles("local")
@Transactional
@DisplayName("웹훅 처리 통합 테스트")
class WebhookIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentHistoryRepository paymentHistoryRepository;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("정상 웹훅 처리 - READY 상태에서 DONE으로 전이")
    void handleWebhook_Success() throws Exception {
        // Given: Payment 준비 (READY 상태, paymentKey 설정됨)
        String paymentKey = "test_payment_key_" + UUID.randomUUID();
        String orderId = "order_" + UUID.randomUUID();

        Payment payment = Payment.builder()
                .orderId(orderId)
                .status(PaymentStatus.READY)
                .method(PaymentMethod.CARD)
                .amount(new BigDecimal("10000"))
                .customerName("테스트 고객")
                .customerEmail("test@example.com")
                .build();
        // 리플렉션으로 paymentKey 설정 (웹훅 테스트를 위해)
        setPaymentKey(payment, paymentKey);
        paymentRepository.save(payment);

        // 웹훅 요청 데이터 생성
        TossWebhookRequest request = createWebhookRequest(paymentKey, orderId, "DONE");
        String requestJson = objectMapper.writeValueAsString(request);

        // When: POST /api/v1/webhooks/toss
        mockMvc.perform(post("/api/v1/webhooks/toss")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("tosspayments-webhook-transmission-id", UUID.randomUUID().toString())
                        .header("tosspayments-webhook-transmission-time", LocalDateTime.now().toString())
                        .header("tosspayments-webhook-transmission-retried-count", "0")
                        .content(requestJson))
                .andExpect(status().isOk());

        // Then: DB 검증: payments.status = DONE
        Payment updatedPayment = paymentRepository.findByOrderId(orderId).orElseThrow();
        assertThat(updatedPayment.getStatus()).isEqualTo(PaymentStatus.DONE);
        assertThat(updatedPayment.getPaymentKey()).isEqualTo(paymentKey);
        assertThat(updatedPayment.getApprovedAt()).isNotNull();

        // DB 검증: payment_histories에 WEBHOOK_PAYMENT_DONE 이력 존재
        List<PaymentHistory> histories = paymentHistoryRepository.findAll();
        assertThat(histories).isNotEmpty();
        assertThat(histories)
                .anyMatch(h -> h.getEventType() == PaymentEventType.WEBHOOK_PAYMENT_DONE);
    }

    @Test
    @DisplayName("멱등성 검증 - 이미 DONE 상태인 경우 중복 처리 방지")
    void handleWebhook_Idempotency() throws Exception {
        // Given: Payment DONE 상태로 저장
        String paymentKey = "test_payment_key_" + UUID.randomUUID();
        String orderId = "order_" + UUID.randomUUID();
        LocalDateTime approvedAt = LocalDateTime.now().minusHours(1);

        Payment payment = Payment.builder()
                .orderId(orderId)
                .status(PaymentStatus.READY)
                .method(PaymentMethod.CARD)
                .amount(new BigDecimal("10000"))
                .customerName("테스트 고객")
                .customerEmail("test@example.com")
                .build();
        payment.approve(paymentKey, approvedAt);
        paymentRepository.save(payment);

        // 초기 이력 개수 확인
        long initialHistoryCount = paymentHistoryRepository.count();

        // 웹훅 요청 데이터 생성 (동일 paymentKey)
        TossWebhookRequest request = createWebhookRequest(paymentKey, orderId, "DONE");
        String requestJson = objectMapper.writeValueAsString(request);

        // When: POST /api/v1/webhooks/toss (중복 요청)
        mockMvc.perform(post("/api/v1/webhooks/toss")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("tosspayments-webhook-transmission-id", UUID.randomUUID().toString())
                        .header("tosspayments-webhook-transmission-time", LocalDateTime.now().toString())
                        .header("tosspayments-webhook-transmission-retried-count", "0")
                        .content(requestJson))
                .andExpect(status().isOk());

        // Then: DB 검증: payments 변경 없음 (여전히 DONE)
        Payment unchangedPayment = paymentRepository.findByOrderId(orderId).orElseThrow();
        assertThat(unchangedPayment.getStatus()).isEqualTo(PaymentStatus.DONE);
        assertThat(unchangedPayment.getPaymentKey()).isEqualTo(paymentKey);
        assertThat(unchangedPayment.getApprovedAt()).isEqualTo(approvedAt);

        // DB 검증: payment_histories에 중복 이력 없음
        long finalHistoryCount = paymentHistoryRepository.count();
        assertThat(finalHistoryCount).isEqualTo(initialHistoryCount);
    }

    @Test
    @DisplayName("Payment 없는 경우 - 200 OK 반환하여 PG사 재시도 방지")
    void handleWebhook_PaymentNotFound() throws Exception {
        // Given: DB에 Payment 없음
        String nonExistentPaymentKey = "non_existent_" + UUID.randomUUID();
        String orderId = "order_" + UUID.randomUUID();

        // 초기 Payment 개수 확인
        long initialPaymentCount = paymentRepository.count();

        // 웹훅 요청 데이터 생성 (존재하지 않는 paymentKey)
        TossWebhookRequest request = createWebhookRequest(nonExistentPaymentKey, orderId, "DONE");
        String requestJson = objectMapper.writeValueAsString(request);

        // When: POST /api/v1/webhooks/toss
        mockMvc.perform(post("/api/v1/webhooks/toss")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("tosspayments-webhook-transmission-id", UUID.randomUUID().toString())
                        .header("tosspayments-webhook-transmission-time", LocalDateTime.now().toString())
                        .header("tosspayments-webhook-transmission-retried-count", "0")
                        .content(requestJson))
                .andExpect(status().isOk());

        // Then: DB 검증: Payment 변경 없음
        long finalPaymentCount = paymentRepository.count();
        assertThat(finalPaymentCount).isEqualTo(initialPaymentCount);
    }

    @Test
    @DisplayName("결제 취소 웹훅 처리 - DONE 상태에서 CANCELED로 전이")
    void handleWebhook_Cancel() throws Exception {
        // Given: Payment DONE 상태로 저장
        String paymentKey = "test_payment_key_" + UUID.randomUUID();
        String orderId = "order_" + UUID.randomUUID();
        LocalDateTime approvedAt = LocalDateTime.now().minusHours(1);

        Payment payment = Payment.builder()
                .orderId(orderId)
                .status(PaymentStatus.READY)
                .method(PaymentMethod.CARD)
                .amount(new BigDecimal("10000"))
                .customerName("테스트 고객")
                .customerEmail("test@example.com")
                .build();
        payment.approve(paymentKey, approvedAt);
        paymentRepository.save(payment);

        // 웹훅 요청 데이터 생성 (status = CANCELED)
        TossWebhookRequest request = createWebhookRequest(paymentKey, orderId, "CANCELED");
        String requestJson = objectMapper.writeValueAsString(request);

        // When: POST /api/v1/webhooks/toss
        mockMvc.perform(post("/api/v1/webhooks/toss")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("tosspayments-webhook-transmission-id", UUID.randomUUID().toString())
                        .header("tosspayments-webhook-transmission-time", LocalDateTime.now().toString())
                        .header("tosspayments-webhook-transmission-retried-count", "0")
                        .content(requestJson))
                .andExpect(status().isOk());

        // Then: DB 검증: payments.status = CANCELED
        Payment canceledPayment = paymentRepository.findByOrderId(orderId).orElseThrow();
        assertThat(canceledPayment.getStatus()).isEqualTo(PaymentStatus.CANCELED);
        assertThat(canceledPayment.getCanceledAt()).isNotNull();

        // DB 검증: payment_histories에 WEBHOOK_PAYMENT_CANCELED 이력 존재
        List<PaymentHistory> histories = paymentHistoryRepository.findAll();
        assertThat(histories).isNotEmpty();
        assertThat(histories)
                .anyMatch(h -> h.getEventType() == PaymentEventType.WEBHOOK_PAYMENT_CANCELED);
    }

    /**
     * Payment에 paymentKey 설정 헬퍼 메서드
     */
    private void setPaymentKey(Payment payment, String paymentKey) {
        try {
            var field = Payment.class.getDeclaredField("paymentKey");
            field.setAccessible(true);
            field.set(payment, paymentKey);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set paymentKey", e);
        }
    }

    /**
     * 웹훅 요청 DTO 생성 헬퍼 메서드
     */
    private TossWebhookRequest createWebhookRequest(String paymentKey, String orderId, String status) {
        TossWebhookRequest request = new TossWebhookRequest();

        // 리플렉션을 사용하여 private 필드 설정
        try {
            var eventTypeField = TossWebhookRequest.class.getDeclaredField("eventType");
            eventTypeField.setAccessible(true);
            eventTypeField.set(request, "PAYMENT_STATUS_CHANGED");

            var createdAtField = TossWebhookRequest.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(request, LocalDateTime.now().toString());

            var dataField = TossWebhookRequest.class.getDeclaredField("data");
            dataField.setAccessible(true);

            var paymentData = new TossWebhookRequest.PaymentData();
            var paymentKeyField = TossWebhookRequest.PaymentData.class.getDeclaredField("paymentKey");
            paymentKeyField.setAccessible(true);
            paymentKeyField.set(paymentData, paymentKey);

            var orderIdField = TossWebhookRequest.PaymentData.class.getDeclaredField("orderId");
            orderIdField.setAccessible(true);
            orderIdField.set(paymentData, orderId);

            var statusField = TossWebhookRequest.PaymentData.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(paymentData, status);

            var totalAmountField = TossWebhookRequest.PaymentData.class.getDeclaredField("totalAmount");
            totalAmountField.setAccessible(true);
            totalAmountField.set(paymentData, new BigDecimal("10000"));

            var methodField = TossWebhookRequest.PaymentData.class.getDeclaredField("method");
            methodField.setAccessible(true);
            methodField.set(paymentData, "CARD");

            var approvedAtField = TossWebhookRequest.PaymentData.class.getDeclaredField("approvedAt");
            approvedAtField.setAccessible(true);
            approvedAtField.set(paymentData, LocalDateTime.now());

            dataField.set(request, paymentData);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create TossWebhookRequest", e);
        }

        return request;
    }

}
