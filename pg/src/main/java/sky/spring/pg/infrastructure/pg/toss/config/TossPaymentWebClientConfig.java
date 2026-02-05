package sky.spring.pg.infrastructure.pg.toss.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

/**
 * 토스페이먼츠 API 호출을 위한 WebClient 설정
 *
 * 이 클래스는 토스페이먼츠와 통신하기 위한 WebClient Bean을 생성합니다.
 * WebClient는 Spring의 논블로킹 HTTP 클라이언트로, 효율적인 비동기 통신을 제공합니다.
 */
@Slf4j
@Configuration
public class TossPaymentWebClientConfig {

    /**
     * 토스페이먼츠 API 호출용 WebClient Bean 생성
     *
     * @param apiUrl 토스페이먼츠 API 기본 URL (application.yml에서 주입)
     * @param secretKey 토스페이먼츠 시크릿 키 (application.yml에서 주입)
     * @return 설정이 완료된 WebClient 인스턴스
     */
    @Bean("tossPaymentWebClient")
    public WebClient tossPaymentWebClient(
            @Value("${payment.toss.api-url}") String apiUrl,
            @Value("${payment.toss.secret-key}") String secretKey
    ) {
        // 1단계: HttpClient 설정 - Timeout 제어
        HttpClient httpClient = createHttpClient();

        // 2단계: Base64 인증 문자열 생성
        String authorizationHeader = createAuthorizationHeader(secretKey);

        // 3단계: WebClient 빌드
        return WebClient.builder()
                .baseUrl(apiUrl)  // 기본 URL 설정 (https://api.tosspayments.com)
                .clientConnector(new ReactorClientHttpConnector(httpClient))  // Netty 기반 HttpClient 연결
                .defaultHeader(HttpHeaders.AUTHORIZATION, authorizationHeader)  // 인증 헤더 추가
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)  // JSON 컨텐츠 타입
                .filter(loggingFilter())  // 요청/응답 로깅 필터
                .build();
    }

    /**
     * HttpClient 생성 - Timeout 설정
     *
     * PG 결제는 네트워크 지연이 발생할 수 있으므로 적절한 Timeout 설정이 중요합니다.
     * - 연결 타임아웃: 10초 (서버에 연결하는 시간)
     * - 읽기/쓰기 타임아웃: 30초 (데이터를 주고받는 시간)
     *
     * @return 설정된 HttpClient
     */
    private HttpClient createHttpClient() {
        return HttpClient.create()
                // 연결 타임아웃: TCP 연결을 맺는데 걸리는 최대 시간
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)  // 10초

                // 응답 타임아웃: 전체 응답을 받는데 걸리는 최대 시간
                .responseTimeout(Duration.ofSeconds(30))

                // 연결이 완료된 후 읽기/쓰기 타임아웃 핸들러 추가
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(30))   // 읽기 타임아웃: 30초
                        .addHandlerLast(new WriteTimeoutHandler(30))  // 쓰기 타임아웃: 30초
                );
    }

    /**
     * Base64 인증 헤더 생성
     *
     * 토스페이먼츠는 Basic Auth 방식을 사용합니다.
     * 형식: "Basic " + Base64(secretKey + ":")
     *
     * 예시:
     * secretKey가 "test_sk_1234"인 경우
     * -> "test_sk_1234:" 를 Base64 인코딩
     * -> "Basic dGVzdF9za18xMjM0Og=="
     *
     * @param secretKey 토스페이먼츠 시크릿 키
     * @return "Basic " + Base64 인코딩된 인증 문자열
     */
    private String createAuthorizationHeader(String secretKey) {
        // 중요: 시크릿 키 뒤에 콜론(:)을 붙여야 합니다!
        String credentials = secretKey + ":";

        // UTF-8로 인코딩 후 Base64 변환
        String encodedCredentials = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        return "Basic " + encodedCredentials;
    }

    /**
     * 요청 로깅 필터
     *
     * API 호출 시 요청 정보를 로그로 남깁니다.
     * 디버깅과 모니터링에 유용합니다.
     *
     * @return 로깅 필터 함수
     */
    private ExchangeFilterFunction loggingFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            log.info("Request: {} {}", request.method(), request.url());

            // 헤더 정보도 로깅 (단, Authorization은 보안상 마스킹)
            request.headers().forEach((name, values) -> {
                if (name.equals(HttpHeaders.AUTHORIZATION)) {
                    log.debug("Header {}: [MASKED]", name);
                } else {
                    log.debug("Header {}: {}", name, values);
                }
            });

            return Mono.just(request);
        });
    }
}
