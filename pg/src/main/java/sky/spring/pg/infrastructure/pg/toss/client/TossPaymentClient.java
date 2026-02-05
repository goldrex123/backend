package sky.spring.pg.infrastructure.pg.toss.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;
import sky.spring.pg.common.exception.PaymentClientException;
import sky.spring.pg.common.exception.PaymentServerException;
import sky.spring.pg.infrastructure.pg.toss.dto.request.TossCancelRequest;
import sky.spring.pg.infrastructure.pg.toss.dto.request.TossPaymentConfirmRequest;
import sky.spring.pg.infrastructure.pg.toss.dto.response.TossCancelResponse;
import sky.spring.pg.infrastructure.pg.toss.dto.response.TossPaymentResponse;

import java.math.BigDecimal;

/**
 * 토스페이먼츠 API 호출 클라이언트
 *
 * 이 클래스는 토스페이먼츠와의 실제 HTTP 통신을 담당하는 Infrastructure Layer 컴포넌트입니다.
 * 비즈니스 로직은 포함하지 않고, 순수하게 외부 API 호출만 수행합니다.
 *
 * 주요 책임:
 * - 결제 승인 API 호출
 * - 결제 취소 API 호출
 * - 결제 조회 API 호출
 * - HTTP 에러를 도메인 예외로 변환 (4xx → PaymentClientException, 5xx → PaymentServerException)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentClient {

    /**
     * 토스페이먼츠 API 호출용 WebClient
     * @Qualifier를 사용하여 명시적으로 Bean 이름을 지정
     */
    @Qualifier("tossPaymentWebClient")
    private final WebClient tossPaymentWebClient;

    /**
     * 결제 승인 요청
     *
     * 사용자가 결제 위젯에서 결제를 완료한 후, 서버에서 최종 승인을 처리하는 API입니다.
     * 이 단계에서 실제 결제가 승인되고, 결제 정보가 확정됩니다.
     *
     * 재시도 정책:
     * - PaymentServerException(5xx 서버 오류)와 네트워크 오류 시 최대 3회 재시도
     * - 재시도 간격: 2초
     * - PaymentClientException(4xx 오류)는 재시도하지 않음
     *
     * @param paymentKey 토스페이먼츠에서 발급한 결제 키
     * @param orderId 가맹점에서 생성한 주문 ID
     * @param amount 결제 금액 (변조 방지를 위해 서버에서 재확인)
     * @return 승인된 결제 정보
     * @throws PaymentClientException 4xx 오류 (잘못된 요청, 이미 승인된 결제 등)
     * @throws PaymentServerException 5xx 오류 (PG사 서버 장애)
     */
    @Retryable(
            retryFor = {PaymentServerException.class, WebClientRequestException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public TossPaymentResponse confirmPayment(
            String paymentKey,
            String orderId,
            BigDecimal amount
    ) {
        log.info("결제 승인 요청 시작 - paymentKey: {}, orderId: {}, amount: {}",
                paymentKey, orderId, amount);

        // 요청 DTO 생성
        TossPaymentConfirmRequest request = TossPaymentConfirmRequest.builder()
                .paymentKey(paymentKey)
                .orderId(orderId)
                .amount(amount)
                .build();

        // WebClient를 사용한 POST 요청
        return tossPaymentWebClient.post()
                .uri("/v1/payments/confirm")
                .bodyValue(request)
                .retrieve()
                // 4xx 에러 처리: 클라이언트 요청 오류 (재시도 불필요)
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        response -> response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("결제 승인 실패 (4xx): {}", body);
                                    return Mono.error(
                                            new PaymentClientException("결제 승인 실패: " + body)
                                    );
                                })
                )
                // 5xx 에러 처리: 서버 오류 (재시도 가능)
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        response -> {
                            log.error("PG사 서버 오류 발생 (5xx)");
                            return Mono.error(
                                    new PaymentServerException("PG사 서버 오류")
                            );
                        }
                )
                .bodyToMono(TossPaymentResponse.class)
                .block();  // 동기 방식으로 변환 (Virtual Threads 환경에서 안전)
    }

    /**
     * 결제 취소 요청
     *
     * 승인된 결제를 취소하는 API입니다.
     * 부분 취소는 지원하지 않으며, 전액 취소만 가능합니다.
     *
     * 재시도 정책:
     * - PaymentServerException(5xx 서버 오류)와 네트워크 오류 시 최대 3회 재시도
     * - 재시도 간격: 2초
     * - PaymentClientException(4xx 오류)는 재시도하지 않음
     *
     * @param paymentKey 취소할 결제의 결제 키
     * @param cancelReason 취소 사유 (예: "고객 요청", "품절")
     * @return 취소 결과 정보
     * @throws PaymentClientException 4xx 오류 (이미 취소된 결제, 취소 불가능한 상태 등)
     * @throws PaymentServerException 5xx 오류 (PG사 서버 장애)
     */
    @Retryable(
            retryFor = {PaymentServerException.class, WebClientRequestException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public TossCancelResponse cancelPayment(String paymentKey, String cancelReason) {
        log.info("결제 취소 요청 시작 - paymentKey: {}, cancelReason: {}",
                paymentKey, cancelReason);

        // 요청 DTO 생성
        TossCancelRequest request = TossCancelRequest.builder()
                .cancelReason(cancelReason)
                .build();

        // WebClient를 사용한 POST 요청 (Path Variable 사용)
        return tossPaymentWebClient.post()
                .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                .bodyValue(request)
                .retrieve()
                // 4xx 에러 처리
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        response -> response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("결제 취소 실패 (4xx): {}", body);
                                    return Mono.error(
                                            new PaymentClientException("결제 취소 실패: " + body)
                                    );
                                })
                )
                // 5xx 에러 처리
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        response -> {
                            log.error("PG사 서버 오류 발생 (5xx)");
                            return Mono.error(
                                    new PaymentServerException("PG사 서버 오류")
                            );
                        }
                )
                .bodyToMono(TossCancelResponse.class)
                .block();
    }

    /**
     * 결제 조회 요청
     *
     * 결제 키로 결제 정보를 조회하는 API입니다.
     * 결제 상태 확인, 결제 정보 동기화 등에 사용됩니다.
     *
     * 재시도 정책:
     * - PaymentServerException(5xx 서버 오류)와 네트워크 오류 시 최대 3회 재시도
     * - 재시도 간격: 2초
     * - PaymentClientException(4xx 오류)는 재시도하지 않음
     *
     * @param paymentKey 조회할 결제의 결제 키
     * @return 결제 정보
     * @throws PaymentClientException 4xx 오류 (존재하지 않는 결제 키 등)
     * @throws PaymentServerException 5xx 오류 (PG사 서버 장애)
     */
    @Retryable(
            retryFor = {PaymentServerException.class, WebClientRequestException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public TossPaymentResponse getPayment(String paymentKey) {
        log.info("결제 조회 요청 시작 - paymentKey: {}", paymentKey);

        // WebClient를 사용한 GET 요청
        return tossPaymentWebClient.get()
                .uri("/v1/payments/{paymentKey}", paymentKey)
                .retrieve()
                // 4xx 에러 처리
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        response -> response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("결제 조회 실패 (4xx): {}", body);
                                    return Mono.error(
                                            new PaymentClientException("결제 조회 실패: " + body)
                                    );
                                })
                )
                // 5xx 에러 처리
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        response -> {
                            log.error("PG사 서버 오류 발생 (5xx)");
                            return Mono.error(
                                    new PaymentServerException("PG사 서버 오류")
                            );
                        }
                )
                .bodyToMono(TossPaymentResponse.class)
                .block();
    }
}
