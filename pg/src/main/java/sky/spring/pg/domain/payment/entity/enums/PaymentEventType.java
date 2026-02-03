package sky.spring.pg.domain.payment.entity.enums;

public enum PaymentEventType {
    PREPARE,                    // 결제 준비
    APPROVE,                    // 결제 승인
    CANCEL,                     // 결제 취소
    FAIL,                       // 결제 실패
    WEBHOOK_PAYMENT_DONE,       // 웹훅: 결제 완료
    WEBHOOK_PAYMENT_CANCELED    // 웹훅: 결제 취소
}
