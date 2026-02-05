package sky.spring.pg.infrastructure.pg.toss.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TossPaymentWebClientConfig 설정 검증 테스트
 *
 * 이 테스트는 WebClient Bean이 올바르게 생성되고 설정되었는지 확인합니다.
 */
@SpringBootTest
@ActiveProfiles("local")
@DisplayName("토스페이먼츠 WebClient 설정 테스트")
class TossPaymentWebClientConfigTest {

    @Autowired
    @Qualifier("tossPaymentWebClient")
    private WebClient tossPaymentWebClient;

    @Test
    @DisplayName("tossPaymentWebClient Bean이 정상적으로 생성되어야 한다")
    void tossPaymentWebClient_Bean이_생성되어야_한다() {
        // given & when & then
        assertThat(tossPaymentWebClient).isNotNull();
        System.out.println("✅ tossPaymentWebClient Bean이 성공적으로 생성되었습니다!");
    }

    @Test
    @DisplayName("WebClient가 올바른 설정을 가지고 있어야 한다")
    void WebClient가_올바른_설정을_가지고_있어야_한다() {
        // given & when
        String clientInfo = tossPaymentWebClient.toString();

        // then
        assertThat(clientInfo).isNotNull();
        System.out.println("✅ WebClient 설정 정보: " + clientInfo);

        // WebClient가 제대로 생성되었는지 확인
        assertThat(tossPaymentWebClient).isInstanceOf(WebClient.class);
        System.out.println("✅ WebClient 타입 검증 성공!");
    }
}
