package sky.spring.pg.infrastructure.pg.toss.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Base64 인증 문자열 생성 검증 테스트
 *
 * 토스페이먼츠 Basic Auth 방식이 올바르게 동작하는지 확인합니다.
 */
@DisplayName("Base64 인증 생성 테스트")
class Base64AuthTest {

    @Test
    @DisplayName("시크릿 키를 Base64로 인코딩할 때 콜론을 포함해야 한다")
    void Base64_인코딩_시_콜론_포함() {
        // given
        String secretKey = "test_sk_1234";

        // when
        String credentials = secretKey + ":";  // 콜론 추가 (중요!)
        String encoded = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        String authHeader = "Basic " + encoded;

        // then
        System.out.println("✅ 원본 시크릿 키: " + secretKey);
        System.out.println("✅ 콜론 포함 문자열: " + credentials);
        System.out.println("✅ Base64 인코딩: " + encoded);
        System.out.println("✅ 최종 Authorization 헤더: " + authHeader);

        // 검증
        assertThat(credentials).endsWith(":");
        assertThat(authHeader).startsWith("Basic ");
        assertThat(encoded).isNotEmpty();

        // Base64 디코딩해서 확인
        String decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
        assertThat(decoded).isEqualTo(credentials);
        System.out.println("✅ Base64 디코딩 검증: " + decoded);
    }

    @Test
    @DisplayName("실제 토스페이먼츠 테스트 키로 Base64 인코딩 확인")
    void 실제_테스트_키_Base64_인코딩() {
        // given
        String testSecretKey = "test_sk_XXXXXXXXXXXXXXXXXXXXX";

        // when
        String credentials = testSecretKey + ":";
        String encoded = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        String authHeader = "Basic " + encoded;

        // then
        System.out.println("=".repeat(60));
        System.out.println("📌 실제 사용할 Authorization 헤더");
        System.out.println("=".repeat(60));
        System.out.println(authHeader);
        System.out.println("=".repeat(60));

        assertThat(authHeader).isNotEmpty();
        assertThat(authHeader).startsWith("Basic ");
    }
}
