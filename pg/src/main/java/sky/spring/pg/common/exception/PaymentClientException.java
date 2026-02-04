package sky.spring.pg.common.exception;

/**
 * PG사 API 호출 시 발생하는 4xx 클라이언트 오류를 나타내는 예외
 * 재시도해도 성공할 수 없는 오류 (잘못된 요청, 인증 실패 등)
 */
public class PaymentClientException extends RuntimeException {

    public PaymentClientException(String message) {
        super(message);
    }
}
