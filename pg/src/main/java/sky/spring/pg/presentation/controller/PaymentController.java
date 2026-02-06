package sky.spring.pg.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sky.spring.pg.application.facade.PaymentFacade;
import sky.spring.pg.common.dto.ApiResponse;
import sky.spring.pg.presentation.dto.request.PaymentApproveRequest;
import sky.spring.pg.presentation.dto.request.PaymentPrepareRequest;
import sky.spring.pg.presentation.dto.response.PaymentApproveResponse;
import sky.spring.pg.presentation.dto.response.PaymentPrepareResponse;

/**
 * 결제 API 컨트롤러
 *
 * 결제 준비 및 승인 엔드포인트를 제공합니다.
 * Bean Validation으로 입력 검증을 수행하고, 일관된 ApiResponse 형식으로 응답합니다.
 *
 * API 엔드포인트:
 * - POST /api/v1/payments/prepare: 결제 준비
 * - POST /api/v1/payments/approve: 결제 승인
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

  private final PaymentFacade paymentFacade;

  /**
   * 결제 준비
   *
   * 새로운 결제를 생성하고 READY 상태로 저장합니다.
   * Request DTO의 필수 필드와 형식이 자동으로 검증됩니다.
   *
   * @param request 결제 준비 요청 (orderId, amount, method 필수)
   * @return 결제 준비 응답 (id, orderId, amount, status, method)
   */
  @PostMapping("/prepare")
  public ResponseEntity<ApiResponse<PaymentPrepareResponse>> preparePayment(
      @Valid @RequestBody PaymentPrepareRequest request
  ) {
    log.info("결제 준비 요청 - orderId: {}, amount: {}",
        request.getOrderId(), request.getAmount());

    PaymentPrepareResponse response = paymentFacade.preparePayment(request);

    return ResponseEntity.ok(ApiResponse.success(response));
  }

  /**
   * 결제 승인
   *
   * PG사로부터 받은 결제 키로 최종 승인을 처리합니다.
   * 금액 검증을 통해 결제 위변조를 방지합니다.
   *
   * @param request 결제 승인 요청 (paymentKey, orderId, amount 필수)
   * @return 결제 승인 응답 (id, paymentKey, orderId, status, amount, approvedAt)
   */
  @PostMapping("/approve")
  public ResponseEntity<ApiResponse<PaymentApproveResponse>> approvePayment(
      @Valid @RequestBody PaymentApproveRequest request
  ) {
    log.info("결제 승인 요청 - orderId: {}, paymentKey: {}",
        request.getOrderId(), request.getPaymentKey());

    PaymentApproveResponse response = paymentFacade.approvePayment(request);

    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
