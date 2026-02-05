package sky.spring.pg.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import sky.spring.pg.common.dto.ApiResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GlobalExceptionHandler 단위 테스트
 *
 * PG 관련 예외가 올바른 HTTP 상태 코드와 응답 포맷으로 처리되는지 검증합니다.
 */
@DisplayName("GlobalExceptionHandler 단위 테스트")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("PaymentClientException은 400 BAD_REQUEST로 처리되어야 한다")
    void shouldHandlePaymentClientExceptionWithBadRequest() {
        // Given
        PaymentClientException exception = new PaymentClientException("잘못된 결제 요청");

        // When
        ResponseEntity<ApiResponse<Void>> response = handler.handlePaymentClientException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getErrorCode()).isEqualTo("PAYMENT_CLIENT_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("잘못된 결제 요청");
    }

    @Test
    @DisplayName("PaymentServerException은 502 BAD_GATEWAY로 처리되어야 한다")
    void shouldHandlePaymentServerExceptionWithBadGateway() {
        // Given
        PaymentServerException exception = new PaymentServerException("PG사 서버 오류");

        // When
        ResponseEntity<ApiResponse<Void>> response = handler.handlePaymentServerException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getErrorCode()).isEqualTo("PAYMENT_SERVER_ERROR");
        assertThat(response.getBody().getMessage()).contains("결제 시스템");
    }

    @Test
    @DisplayName("일반 Exception은 500 INTERNAL_SERVER_ERROR로 처리되어야 한다")
    void shouldHandleGeneralExceptionWithInternalServerError() {
        // Given
        Exception exception = new RuntimeException("예상치 못한 오류");

        // When
        ResponseEntity<ApiResponse<Void>> response = handler.handleException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getErrorCode()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("서버 오류가 발생했습니다.");
    }

    @Test
    @DisplayName("모든 에러 응답은 ApiResponse 구조를 따라야 한다")
    void allErrorResponsesShouldFollowApiResponseStructure() {
        // Given
        PaymentClientException clientException = new PaymentClientException("클라이언트 오류");
        PaymentServerException serverException = new PaymentServerException("서버 오류");

        // When
        ResponseEntity<ApiResponse<Void>> clientResponse = handler.handlePaymentClientException(clientException);
        ResponseEntity<ApiResponse<Void>> serverResponse = handler.handlePaymentServerException(serverException);

        // Then
        // 모든 응답이 ApiResponse 타입이어야 함
        assertThat(clientResponse.getBody()).isInstanceOf(ApiResponse.class);
        assertThat(serverResponse.getBody()).isInstanceOf(ApiResponse.class);

        // 모든 응답이 timestamp를 가져야 함
        assertThat(clientResponse.getBody().getTimestamp()).isNotNull();
        assertThat(serverResponse.getBody().getTimestamp()).isNotNull();

        // success는 모두 false여야 함
        assertThat(clientResponse.getBody().isSuccess()).isFalse();
        assertThat(serverResponse.getBody().isSuccess()).isFalse();
    }

    @Test
    @DisplayName("에러 코드는 대문자 스네이크 케이스여야 한다")
    void errorCodesShouldBeUpperSnakeCase() {
        // Given
        PaymentClientException clientException = new PaymentClientException("테스트");
        PaymentServerException serverException = new PaymentServerException("테스트");

        // When
        ResponseEntity<ApiResponse<Void>> clientResponse = handler.handlePaymentClientException(clientException);
        ResponseEntity<ApiResponse<Void>> serverResponse = handler.handlePaymentServerException(serverException);

        // Then
        assertThat(clientResponse.getBody().getErrorCode()).matches("^[A-Z_]+$");
        assertThat(serverResponse.getBody().getErrorCode()).matches("^[A-Z_]+$");
    }
}
