package sky.spring.pg.common.exception;

/**
 * 결제 금액이 불일치할 때 발생하는 예외
 *
 * 클라이언트가 요청한 금액과 서버에 저장된 금액이 다를 때 발생합니다.
 * 결제 위변조 방지를 위한 필수 검증입니다.
 */
public class PaymentAmountMismatchException extends RuntimeException {

  public PaymentAmountMismatchException(String message) {
    super(message);
  }
}
