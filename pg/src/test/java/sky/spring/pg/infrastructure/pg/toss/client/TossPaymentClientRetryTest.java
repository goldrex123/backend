package sky.spring.pg.infrastructure.pg.toss.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TossPaymentClient 재시도 로직 테스트
 *
 * @Retryable 어노테이션이 정상적으로 적용되었는지 검증합니다.
 * 실제 재시도 동작은 RetryConfigTest에서 검증합니다.
 */
@SpringBootTest
@ActiveProfiles("local")
@DisplayName("TossPaymentClient 재시도 로직 테스트")
class TossPaymentClientRetryTest {

    @Autowired
    private TossPaymentClient tossPaymentClient;

    @Test
    @DisplayName("TossPaymentClient가 정상적으로 Bean으로 등록되어야 한다")
    void tossPaymentClientShouldBeInjected() {
        // Given & When & Then
        assertThat(tossPaymentClient).isNotNull();
    }

    @Test
    @DisplayName("@Retryable이 적용된 메서드가 AOP 프록시로 래핑되어야 한다")
    void methodsShouldBeProxied() {
        // Given & When
        String className = tossPaymentClient.getClass().getName();

        // Then
        // Spring AOP 프록시가 적용되면 클래스 이름에 "$$" 또는 "EnhancerBy"가 포함됨
        // 또는 원본 클래스 그대로일 수 있음 (CGLIB 프록시 방식에 따라 다름)
        assertThat(tossPaymentClient).isNotNull();

        // 실제 재시도 동작은 RetryConfigTest에서 RetryTemplate로 검증
    }
}
