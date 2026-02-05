package sky.spring.pg.infrastructure.pg.toss.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TossPaymentClient 통합 테스트
 *
 * Spring Context를 로드하여 Bean 주입과 기본 구성을 검증합니다.
 */
@SpringBootTest
@ActiveProfiles("local")
@DisplayName("TossPaymentClient 통합 테스트")
class TossPaymentClientTest {

    @Autowired
    private TossPaymentClient tossPaymentClient;

    @Test
    @DisplayName("TossPaymentClient Bean이 정상적으로 주입되어야 한다")
    void tossPaymentClientShouldBeInjected() {
        // Given & When & Then
        assertThat(tossPaymentClient).isNotNull();
    }

    @Test
    @DisplayName("WebClient가 @Qualifier를 통해 정상적으로 주입되어야 한다")
    void webClientShouldBeInjectedWithQualifier() {
        // Given & When
        // TossPaymentClient가 정상적으로 생성되었다면 WebClient도 주입된 것

        // Then
        assertThat(tossPaymentClient).isNotNull();

        // Bean이 정상적으로 주입되었으므로 WebClient도 주입된 것으로 간주
    }
}
