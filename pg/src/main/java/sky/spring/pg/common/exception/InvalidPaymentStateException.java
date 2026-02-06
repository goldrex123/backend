package sky.spring.pg.common.exception;

/**
 * 결제 상태가 승인 불가능할 때 발생하는 예외
 *
 * 이미 승인된 결제를 재승인하거나, 취소된 결제를 승인하는 등
 * 비즈니스 규칙상 허용되지 않는 상태 전이를 시도할 때 발생합니다.
 */
public class InvalidPaymentStateException extends RuntimeException {

  public InvalidPaymentStateException(String message) {
    super(message);
  }
}
