package sky.spring.pg.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.classify.Classifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import sky.spring.pg.common.exception.PaymentClientException;
import sky.spring.pg.common.exception.PaymentServerException;

import java.util.HashMap;
import java.util.Map;

/**
 * Spring Retry 설정
 *
 * 네트워크 오류나 PG사 서버 오류 시 자동 재시도를 위한 설정입니다.
 *
 * 재시도 대상:
 * - PaymentServerException: 5xx 서버 오류 (일시적 장애로 복구 가능)
 * - WebClientRequestException: 네트워크 오류 (연결 실패, 타임아웃 등)
 *
 * 재시도 제외:
 * - PaymentClientException: 4xx 클라이언트 오류 (요청 자체가 잘못되어 재시도 무의미)
 */
@Slf4j
@Configuration
@EnableRetry
public class RetryConfig {

    /**
     * RetryTemplate Bean 생성
     *
     * 프로그래밍 방식으로 재시도 로직을 적용할 때 사용합니다.
     * @Retryable 어노테이션을 사용할 수 없는 경우에 유용합니다.
     *
     * 설정:
     * - 최대 3회 재시도 (초기 시도 포함 총 4회 시도)
     * - 재시도 간격: 2초 고정
     * - 재시도 대상: PaymentServerException, WebClientRequestException
     *
     * @return 설정된 RetryTemplate 인스턴스
     */
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // 1. 재시도 정책 설정
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(PaymentServerException.class, true);           // 5xx 서버 오류
        retryableExceptions.put(WebClientRequestException.class, true);        // 네트워크 오류
        retryableExceptions.put(PaymentClientException.class, false);          // 4xx 클라이언트 오류 (재시도 안 함)

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(
                4,  // 최대 시도 횟수 (초기 시도 1회 + 재시도 3회)
                retryableExceptions
        );

        // 2. 백오프 정책 설정 (재시도 간격)
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(2000L);  // 2초 대기

        // 3. RetryTemplate에 정책 적용
        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        // 4. 재시도 리스너 등록 (로깅용)
        retryTemplate.registerListener(new org.springframework.retry.RetryListener() {
            @Override
            public <T, E extends Throwable> void onError(
                    org.springframework.retry.RetryContext context,
                    org.springframework.retry.RetryCallback<T, E> callback,
                    Throwable throwable
            ) {
                log.warn("재시도 발생 - 시도 횟수: {}, 예외: {}",
                        context.getRetryCount(),
                        throwable.getClass().getSimpleName());
            }
        });

        log.info("RetryTemplate Bean 생성 완료 - 최대 재시도 3회, 백오프 간격 2초");
        return retryTemplate;
    }
}
