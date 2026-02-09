package sky.spring.pg.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import sky.spring.pg.application.facade.PaymentFacade;
import sky.spring.pg.domain.payment.repository.PaymentCancelRepository;
import sky.spring.pg.domain.payment.repository.PaymentRepository;
import sky.spring.pg.domain.payment.service.PaymentService;
import sky.spring.pg.infrastructure.pg.toss.client.TossPaymentClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 결제 취소 통합 테스트
 *
 * Spring Boot Context 로딩 및 Bean 의존성 주입을 검증합니다.
 * 실제 PG API를 호출하는 테스트는 별도의 E2E 테스트에서 수행합니다.
 */
@SpringBootTest
@ActiveProfiles("local")
@DisplayName("결제 취소 통합 테스트")
class PaymentCancelIntegrationTest {

    @Autowired
    private PaymentFacade paymentFacade;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentCancelRepository paymentCancelRepository;

    @Autowired
    private TossPaymentClient tossPaymentClient;

    @Test
    @DisplayName("Spring Context 로딩 및 Bean 주입 확인")
    void contextLoads() {
        // Given & When & Then
        assertThat(paymentFacade).isNotNull();
        assertThat(paymentService).isNotNull();
        assertThat(paymentRepository).isNotNull();
        assertThat(paymentCancelRepository).isNotNull();
        assertThat(tossPaymentClient).isNotNull();
    }

    @Test
    @DisplayName("PaymentCancelRepository의 findByPaymentId 메서드 존재 확인")
    void paymentCancelRepositoryMethodExists() {
        // Given & When & Then
        // 메서드가 존재하는지 리플렉션으로 확인
        try {
            PaymentCancelRepository.class.getMethod("findByPaymentId", Long.class);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("findByPaymentId 메서드가 존재하지 않습니다", e);
        }
    }
}
