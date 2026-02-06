package sky.spring.pg.domain.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sky.spring.pg.common.exception.DuplicatePaymentException;
import sky.spring.pg.common.exception.InvalidPaymentStateException;
import sky.spring.pg.common.exception.PaymentAmountMismatchException;
import sky.spring.pg.common.exception.PaymentNotFoundException;
import sky.spring.pg.common.util.JsonUtil;
import sky.spring.pg.domain.payment.entity.Payment;
import sky.spring.pg.domain.payment.entity.PaymentHistory;
import sky.spring.pg.domain.payment.entity.enums.PaymentEventType;
import sky.spring.pg.domain.payment.entity.enums.PaymentStatus;
import sky.spring.pg.domain.payment.repository.PaymentHistoryRepository;
import sky.spring.pg.domain.payment.repository.PaymentRepository;
import sky.spring.pg.infrastructure.pg.toss.dto.response.TossPaymentResponse;
import sky.spring.pg.presentation.dto.request.PaymentPrepareRequest;
import sky.spring.pg.presentation.dto.response.PaymentApproveResponse;
import sky.spring.pg.presentation.dto.response.PaymentPrepareResponse;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 결제 도메인 서비스
 *
 * 결제 준비, 승인 후 상태 업데이트 등 결제 관련 비즈니스 로직을 처리합니다.
 * 비관적 락을 사용한 동시성 제어와 멱등성 보장을 제공합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PaymentService {

  private final PaymentRepository paymentRepository;
  private final PaymentHistoryRepository paymentHistoryRepository;
  private final JsonUtil jsonUtil;

  /**
   * 결제 준비
   *
   * 새로운 결제를 생성하고 READY 상태로 저장합니다.
   * 중복된 orderId는 DuplicatePaymentException을 발생시킵니다.
   *
   * @param request 결제 준비 요청
   * @return 결제 준비 응답
   * @throws DuplicatePaymentException 중복된 orderId인 경우
   */
  @Transactional
  public PaymentPrepareResponse preparePayment(PaymentPrepareRequest request) {
    // 1. 중복 체크 (멱등성)
    Optional<Payment> existingPayment = paymentRepository.findByOrderId(request.getOrderId());
    if (existingPayment.isPresent()) {
      throw new DuplicatePaymentException(
          String.format("이미 존재하는 주문입니다. orderId: %s", request.getOrderId())
      );
    }

    // 2. Payment 엔티티 생성 (상태: READY)
    Payment payment = Payment.builder()
        .orderId(request.getOrderId())
        .status(PaymentStatus.READY)
        .method(request.getMethod())
        .amount(request.getAmount())
        .customerName(request.getCustomerName())
        .customerEmail(request.getCustomerEmail())
        .build();

    paymentRepository.save(payment);
    log.info("결제 준비 완료 - orderId: {}, amount: {}", payment.getOrderId(), payment.getAmount());

    // 3. 이력 저장
    saveHistory(payment, PaymentEventType.PREPARE, request, null);

    return PaymentPrepareResponse.from(payment);
  }

  /**
   * 결제 승인 후 상태 업데이트
   *
   * PG사로부터 받은 승인 결과를 바탕으로 결제 상태를 업데이트합니다.
   * 비관적 락을 사용하여 동시성을 제어하고, 멱등성을 보장합니다.
   *
   * @param orderId 주문 ID
   * @param requestAmount 요청 금액
   * @param tossResponse 토스페이먼츠 응답
   * @return 결제 승인 응답
   * @throws PaymentNotFoundException 결제 정보가 없는 경우
   * @throws InvalidPaymentStateException 결제 가능한 상태가 아닌 경우
   * @throws PaymentAmountMismatchException 금액이 일치하지 않는 경우
   */
  @Transactional
  public PaymentApproveResponse updatePaymentAfterApproval(
      String orderId,
      BigDecimal requestAmount,
      TossPaymentResponse tossResponse
  ) {
    // 1. 비관적 락으로 조회 (동시성 제어)
    Payment payment = paymentRepository.findByOrderIdWithLock(orderId)
        .orElseThrow(() -> new PaymentNotFoundException(
            String.format("결제 정보를 찾을 수 없습니다. orderId: %s", orderId)
        ));

    // 2. 멱등성 체크 (이미 승인된 경우 기존 결과 반환)
    if (payment.getStatus() == PaymentStatus.DONE) {
      log.info("이미 승인된 결제 - orderId: {}", orderId);
      return PaymentApproveResponse.from(payment);
    }

    // 3. 상태 검증
    if (payment.getStatus() != PaymentStatus.READY) {
      throw new InvalidPaymentStateException(
          String.format("결제 가능한 상태가 아닙니다. 현재 상태: %s", payment.getStatus())
      );
    }

    // 4. 금액 검증
    if (!payment.getAmount().equals(requestAmount)) {
      throw new PaymentAmountMismatchException(
          String.format("결제 금액 불일치. 요청: %s, 실제: %s", requestAmount, payment.getAmount())
      );
    }

    // 5. Payment 상태 업데이트 (Entity 메서드 사용)
    payment.approve(tossResponse.getPaymentKey(), tossResponse.getApprovedAt());
    log.info("결제 승인 완료 - orderId: {}, paymentKey: {}", orderId, tossResponse.getPaymentKey());

    // 6. 이력 저장
    saveHistory(payment, PaymentEventType.APPROVE, tossResponse, 200);

    return PaymentApproveResponse.from(payment);
  }

  /**
   * 결제 이력 저장
   *
   * 결제 관련 이벤트를 PaymentHistory에 기록합니다.
   *
   * @param payment 결제 엔티티
   * @param eventType 이벤트 타입
   * @param data 요청/응답 데이터
   * @param statusCode HTTP 상태 코드
   */
  private void saveHistory(Payment payment, PaymentEventType eventType, Object data, Integer statusCode) {
    PaymentHistory history = PaymentHistory.builder()
        .payment(payment)
        .eventType(eventType)
        .requestBody(jsonUtil.toJson(data))
        .statusCode(statusCode)
        .build();

    paymentHistoryRepository.save(history);
  }
}
