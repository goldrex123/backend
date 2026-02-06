package sky.spring.pg.common.exception;

/**
 * 결제 정보를 찾을 수 없을 때 발생하는 예외
 *
 * 존재하지 않는 orderId 또는 paymentKey로 결제를 조회할 때 발생합니다.
 */
public class PaymentNotFoundException extends RuntimeException {

  public PaymentNotFoundException(String message) {
    super(message);
  }
}
