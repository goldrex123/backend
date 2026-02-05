package sky.spring.pg.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.context.ActiveProfiles;
import sky.spring.pg.common.exception.PaymentClientException;
import sky.spring.pg.common.exception.PaymentServerException;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * RetryConfig 통합 테스트
 *
 * Spring Retry 설정이 정상적으로 Bean으로 등록되고 동작하는지 검증합니다.
 */
@SpringBootTest
@ActiveProfiles("local")
@DisplayName("RetryConfig 통합 테스트")
class RetryConfigTest {

    @Autowired
    private RetryTemplate retryTemplate;

    @Test
    @DisplayName("RetryTemplate Bean이 정상적으로 생성되어야 한다")
    void retryTemplateShouldBeCreated() {
        // Given & When & Then
        assertThat(retryTemplate).isNotNull();
    }

    @Test
    @DisplayName("PaymentServerException 발생 시 최대 3회 재시도해야 한다")
    void shouldRetryThreeTimesOnPaymentServerException() {
        // Given
        AtomicInteger attemptCount = new AtomicInteger(0);

        // When & Then
        assertThatThrownBy(() ->
                retryTemplate.execute(context -> {
                    attemptCount.incrementAndGet();
                    throw new PaymentServerException("서버 오류");
                })
        ).isInstanceOf(PaymentServerException.class);

        // 초기 시도 1회 + 재시도 3회 = 총 4회 시도
        assertThat(attemptCount.get()).isEqualTo(4);
    }

    @Test
    @DisplayName("PaymentClientException 발생 시 재시도하지 않아야 한다")
    void shouldNotRetryOnPaymentClientException() {
        // Given
        AtomicInteger attemptCount = new AtomicInteger(0);

        // When & Then
        assertThatThrownBy(() ->
                retryTemplate.execute(context -> {
                    attemptCount.incrementAndGet();
                    throw new PaymentClientException("클라이언트 오류");
                })
        ).isInstanceOf(PaymentClientException.class);

        // 재시도하지 않으므로 1회만 시도
        assertThat(attemptCount.get()).isEqualTo(1);
    }
}
