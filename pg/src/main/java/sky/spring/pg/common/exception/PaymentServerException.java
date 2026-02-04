package sky.spring.pg.common.exception;

/**
 * PG사 API 호출 시 발생하는 5xx 서버 오류를 나타내는 예외
 * 일시적인 장애로 재시도 시 성공할 수 있는 오류
 */
public class PaymentServerException extends RuntimeException {

    public PaymentServerException(String message) {
        super(message);
    }
}
