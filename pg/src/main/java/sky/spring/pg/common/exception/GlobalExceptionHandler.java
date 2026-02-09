package sky.spring.pg.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sky.spring.pg.common.dto.ApiResponse;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * PG 클라이언트 오류 처리 (4xx)
     *
     * 토스페이먼츠 API 호출 시 발생하는 클라이언트 오류를 처리합니다.
     * 잘못된 요청, 중복된 주문 ID, 유효하지 않은 결제 키 등이 포함됩니다.
     *
     * @param e PaymentClientException
     * @return 400 BAD_REQUEST 응답
     */
    @ExceptionHandler(PaymentClientException.class)
    public ResponseEntity<ApiResponse<Void>> handlePaymentClientException(
            PaymentClientException e
    ) {
        log.warn("PG 클라이언트 오류 발생: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("PAYMENT_CLIENT_ERROR", e.getMessage()));
    }

    /**
     * PG 서버 오류 처리 (5xx)
     *
     * 토스페이먼츠 API 서버의 일시적 장애나 오류를 처리합니다.
     * 이 오류는 재시도 로직을 통해 복구를 시도하지만, 최종적으로 실패한 경우입니다.
     *
     * @param e PaymentServerException
     * @return 502 BAD_GATEWAY 응답
     */
    @ExceptionHandler(PaymentServerException.class)
    public ResponseEntity<ApiResponse<Void>> handlePaymentServerException(
            PaymentServerException e
    ) {
        log.error("PG 서버 오류 발생: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(ApiResponse.error("PAYMENT_SERVER_ERROR", "결제 시스템 일시적 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
    }

    /**
     * 결제 정보 미존재 처리 (404)
     *
     * 존재하지 않는 orderId 또는 paymentKey로 결제를 조회할 때 발생합니다.
     *
     * @param e PaymentNotFoundException
     * @return 404 NOT_FOUND 응답
     */
    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handlePaymentNotFoundException(
            PaymentNotFoundException e
    ) {
        log.warn("결제 정보를 찾을 수 없음: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("PAYMENT_NOT_FOUND", e.getMessage()));
    }

    /**
     * 잘못된 결제 상태 처리 (400)
     *
     * 비즈니스 규칙상 허용되지 않는 상태 전이를 시도할 때 발생합니다.
     * 예: 이미 승인된 결제를 재승인, 취소된 결제를 승인 등
     *
     * @param e InvalidPaymentStateException
     * @return 400 BAD_REQUEST 응답
     */
    @ExceptionHandler(InvalidPaymentStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidPaymentStateException(
            InvalidPaymentStateException e
    ) {
        log.warn("잘못된 결제 상태: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("INVALID_PAYMENT_STATE", e.getMessage()));
    }

    /**
     * 결제 금액 불일치 처리 (400)
     *
     * 클라이언트가 요청한 금액과 서버에 저장된 금액이 다를 때 발생합니다.
     * 결제 위변조 방지를 위한 필수 검증입니다.
     *
     * @param e PaymentAmountMismatchException
     * @return 400 BAD_REQUEST 응답
     */
    @ExceptionHandler(PaymentAmountMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handlePaymentAmountMismatchException(
            PaymentAmountMismatchException e
    ) {
        log.warn("결제 금액 불일치: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("PAYMENT_AMOUNT_MISMATCH", e.getMessage()));
    }

    /**
     * 취소 금액 초과 처리 (400)
     *
     * 취소 가능한 금액을 초과하여 취소를 요청할 때 발생합니다.
     * 부분 취소 시 남은 금액보다 큰 금액으로 취소를 시도하거나,
     * 이미 전액 취소된 결제를 다시 취소하려는 경우입니다.
     *
     * @param e InvalidCancelAmountException
     * @return 400 BAD_REQUEST 응답
     */
    @ExceptionHandler(InvalidCancelAmountException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCancelAmountException(
            InvalidCancelAmountException e
    ) {
        log.warn("취소 금액 초과: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("INVALID_CANCEL_AMOUNT", e.getMessage()));
    }

    /**
     * 중복 결제 시도 처리 (409)
     *
     * 동일한 orderId로 이미 결제가 생성되어 있을 때 발생합니다.
     * 멱등성 보장을 위한 필수 검증입니다.
     *
     * @param e DuplicatePaymentException
     * @return 409 CONFLICT 응답
     */
    @ExceptionHandler(DuplicatePaymentException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicatePaymentException(
            DuplicatePaymentException e
    ) {
        log.warn("중복 결제 시도: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("DUPLICATE_PAYMENT", e.getMessage()));
    }

    /**
     * 일반 예외 처리 (Fallback)
     *
     * 위의 특정 예외 핸들러에서 처리되지 않은 모든 예외를 처리합니다.
     * 이 핸들러는 최후의 안전망 역할을 합니다.
     *
     * @param e Exception
     * @return 500 INTERNAL_SERVER_ERROR 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("예상하지 못한 예외 발생", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다."));
    }
}
