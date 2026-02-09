package sky.spring.pg.common.exception;

/**
 * 취소 금액이 유효하지 않을 때 발생하는 예외
 *
 * 취소 가능한 금액을 초과하여 취소를 요청하거나,
 * 유효하지 않은 금액(음수, 0 등)으로 취소를 시도할 때 발생합니다.
 */
public class InvalidCancelAmountException extends RuntimeException {

  public InvalidCancelAmountException(String message) {
    super(message);
  }
}
