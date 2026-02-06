package sky.spring.pg.common.exception;

/**
 * 중복된 주문 ID로 결제를 시도할 때 발생하는 예외
 *
 * 동일한 orderId로 이미 결제가 생성되어 있을 때 발생합니다.
 * 멱등성 보장을 위한 필수 검증입니다.
 */
public class DuplicatePaymentException extends RuntimeException {

  public DuplicatePaymentException(String message) {
    super(message);
  }
}
