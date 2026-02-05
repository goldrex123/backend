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
