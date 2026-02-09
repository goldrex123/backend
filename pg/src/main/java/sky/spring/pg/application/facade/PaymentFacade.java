package sky.spring.pg.application.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sky.spring.pg.common.exception.PaymentClientException;
import sky.spring.pg.common.exception.PaymentServerException;
import sky.spring.pg.domain.payment.entity.Payment;
import sky.spring.pg.domain.payment.service.PaymentService;
import sky.spring.pg.infrastructure.pg.toss.client.TossPaymentClient;
import sky.spring.pg.infrastructure.pg.toss.dto.response.TossCancelResponse;
import sky.spring.pg.infrastructure.pg.toss.dto.response.TossPaymentResponse;
import sky.spring.pg.presentation.dto.request.PaymentApproveRequest;
import sky.spring.pg.presentation.dto.request.PaymentCancelRequest;
import sky.spring.pg.presentation.dto.request.PaymentPrepareRequest;
import sky.spring.pg.presentation.dto.response.PaymentApproveResponse;
import sky.spring.pg.presentation.dto.response.PaymentCancelResponse;
import sky.spring.pg.presentation.dto.response.PaymentPrepareResponse;

/**
 * 결제 프로세스 조율 Facade
 *
 * PG API 호출과 DB 트랜잭션을 분리하여 성능을 최적화합니다.
 * 트랜잭션은 Service 계층에서만 관리되며, Facade는 트랜잭션을 사용하지 않습니다.
 *
 * 핵심 책임:
 * - 결제 준비: PaymentService 호출
 * - 결제 승인: TossPaymentClient 호출 → PaymentService 호출 (순차 실행)
 * - 로깅 및 예외 전파
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentFacade {

  private final PaymentService paymentService;
  private final TossPaymentClient tossPaymentClient;

  /**
   * 결제 준비
   *
   * PaymentService를 호출하여 결제를 준비합니다.
   * PG API 호출이 없으므로 단순히 Service를 위임합니다.
   *
   * @param request 결제 준비 요청
   * @return 결제 준비 응답
   */
  public PaymentPrepareResponse preparePayment(PaymentPrepareRequest request) {
    log.info("결제 준비 Facade 호출 - orderId: {}", request.getOrderId());
    return paymentService.preparePayment(request);
  }

  /**
   * 결제 승인
   *
   * 1. TossPaymentClient를 호출하여 PG사 승인 요청 (트랜잭션 밖)
   * 2. PaymentService를 호출하여 DB 업데이트 (짧은 트랜잭션)
   *
   * 이렇게 분리하면:
   * - 네트워크 지연으로 인한 DB 커넥션 점유 시간 최소화
   * - PG API 호출 실패 시 DB 트랜잭션이 시작되지 않음
   *
   * @param request 결제 승인 요청
   * @return 결제 승인 응답
   * @throws PaymentClientException PG API 클라이언트 오류 (4xx)
   * @throws PaymentServerException PG API 서버 오류 (5xx)
   */
  public PaymentApproveResponse approvePayment(PaymentApproveRequest request) {
    log.info("결제 승인 Facade 호출 - orderId: {}, paymentKey: {}",
        request.getOrderId(), request.getPaymentKey());

    // 1. PG사 승인 요청 (트랜잭션 밖에서 실행)
    // 네트워크 호출이므로 트랜잭션에 포함하지 않음
    TossPaymentResponse tossResponse;
    try {
      tossResponse = tossPaymentClient.confirmPayment(
          request.getPaymentKey(),
          request.getOrderId(),
          request.getAmount()
      );
      log.info("PG 승인 성공 - orderId: {}, tossStatus: {}",
          request.getOrderId(), tossResponse.getStatus());
    } catch (PaymentClientException | PaymentServerException e) {
      log.error("PG 승인 실패 - orderId: {}, error: {}", request.getOrderId(), e.getMessage());
      throw e;
    }

    // 2. DB 업데이트 (짧은 트랜잭션, Service에서 처리)
    return paymentService.updatePaymentAfterApproval(
        request.getOrderId(),
        request.getAmount(),
        tossResponse
    );
  }

  /**
   * 결제 취소
   *
   * 1. Payment 조회하여 paymentKey 획득 (읽기 전용)
   * 2. TossPaymentClient를 호출하여 PG사 취소 요청 (트랜잭션 밖)
   * 3. PaymentService를 호출하여 DB 업데이트 (짧은 트랜잭션)
   *
   * 이렇게 분리하면:
   * - 네트워크 지연으로 인한 DB 커넥션 점유 시간 최소화
   * - PG API 호출 실패 시 DB 트랜잭션이 시작되지 않음
   *
   * @param request 결제 취소 요청
   * @return 결제 취소 응답
   * @throws PaymentClientException PG API 클라이언트 오류 (4xx)
   * @throws PaymentServerException PG API 서버 오류 (5xx)
   */
  public PaymentCancelResponse cancelPayment(PaymentCancelRequest request) {
    log.info("결제 취소 Facade 호출 - orderId: {}, cancelAmount: {}",
        request.getOrderId(), request.getCancelAmount());

    // 1. Payment 조회하여 paymentKey 획득 (읽기 전용)
    // Note: 취소는 반드시 DONE 상태 결제만 가능하므로 paymentKey 존재 보장
    Payment payment = paymentService.getPaymentByOrderId(request.getOrderId());

    // 2. PG사 취소 요청 (트랜잭션 밖에서 실행)
    TossCancelResponse tossResponse;
    try {
      tossResponse = tossPaymentClient.cancelPayment(
          payment.getPaymentKey(),
          request.getCancelAmount(),
          request.getCancelReason()
      );
      log.info("PG 취소 성공 - orderId: {}, cancelAmount: {}",
          request.getOrderId(), tossResponse.getCancelAmount());
    } catch (PaymentClientException | PaymentServerException e) {
      log.error("PG 취소 실패 - orderId: {}, error: {}",
          request.getOrderId(), e.getMessage());
      throw e;
    }

    // 3. DB 업데이트 (짧은 트랜잭션, Service에서 처리)
    return paymentService.cancelPayment(
        request.getOrderId(),
        request.getCancelAmount(),
        request.getCancelReason()
    );
  }
}
