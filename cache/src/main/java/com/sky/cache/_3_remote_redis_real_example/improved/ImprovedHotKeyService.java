package com.sky.cache._3_remote_redis_real_example.improved;

import com.sky.cache._1_practice.Book;
import com.sky.cache._1_practice.BookRepository;
import com.sky.cache._3_remote_redis_real_example.DbCallChecker;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.awt.desktop.ScreenSleepEvent;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class ImprovedHotKeyService {

    private static final String SERVICE_NAME = "ImprovedHotKeyService";
    private static final String NULL_VALUE = "__NULL__";

    private static final long TTL_BASE = 500;
    private static final long JITTER_RANGE = 10;

    private static final long LOCK_TIMEOUT_MS = 400;
    private static final int MAX_RETRY_COUNT = 40;
    private static final long RETRY_DELAY_MS = 5;
    private static final long RETRY_DELAY_JITTER_MS = 15;

    private final BookRepository bookRepository;
    private final RedisTemplate<String,String> redisTemplate;
    private final DistributedLockProvider distributedLockProvider;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    private final DbCallChecker dbCallChecker = new DbCallChecker("Hot Key + 락 적용");

    public Book findBookByIdWithLock(Long id) {
        String key = id.toString();
        String cached = redisTemplate.opsForValue().get(key);

        // 2. 캐시에 존재하지만 NULL_VALUE가 저장되어 있다면 (데이터가 없음을 의미)
        if (cached != null && cached.equals(NULL_VALUE)) {
            meterRegistry.counter("cache.hit", "service", SERVICE_NAME).increment();
            return null;
        }

        // 3. 캐시에 존재하지만 NULL_VALUE 는 아닌 경우
        if (cached != null) {
            meterRegistry.counter("cache.hit", "service", SERVICE_NAME).increment();
            return objectMapper.readValue(cached, new TypeReference<>() {
            });
        }

        meterRegistry.counter("cache.miss", "service", SERVICE_NAME).increment();

        String lockKey = makeLockKey(id);
        for (int retry = 0; retry < MAX_RETRY_COUNT; retry++) {
            boolean lockAcquired = distributedLockProvider.tryLock(lockKey, LOCK_TIMEOUT_MS);

            if (!lockAcquired) {
                log.warn(">> 경쟁 발생으로 락 획득 실패! - key={}, retry={}", lockKey, retry);
                long jitter = ThreadLocalRandom.current().nextLong(RETRY_DELAY_JITTER_MS);
                sleep(RETRY_DELAY_MS + jitter);
                continue;
            }

            try {
                cached = redisTemplate.opsForValue().get(key);
                if (cached != null && cached.equals(NULL_VALUE)) {
                    meterRegistry.counter("cache.hit", "service", SERVICE_NAME).increment();
                    return null;
                }

                if (cached != null) {
                    meterRegistry.counter("cache.hit", "service", SERVICE_NAME).increment();
                    return objectMapper.readValue(cached, new TypeReference<>() {
                    });
                }

                Book bookFromDb = bookRepository.findById(id).orElse(null);
                dbCallChecker.incrementDbSelectCount();

                if (bookFromDb == null) {
                    setCacheWithJitter(key, NULL_VALUE);
                    return null;
                }

                String serializedBookFromDb = objectMapper.writeValueAsString(bookFromDb);
                setCacheWithJitter(key,serializedBookFromDb);
                return bookFromDb;
            } finally {
                distributedLockProvider.releaseLock(lockKey);
            }
        }

        throw new IllegalStateException("Failed to acquire lock after " + MAX_RETRY_COUNT + " - key=" + lockKey);
    }


    private void setCacheWithJitter(String key, String value) {
        long ttl = TTL_BASE + ThreadLocalRandom.current().nextLong(JITTER_RANGE);
        redisTemplate.opsForValue().set(key, value, ttl, TimeUnit.MILLISECONDS);
    }

    private String makeLockKey(Long id) {
        return "lock-book:" + id;
    }

    private void sleep(long ms) {
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }

    public void logDbCall() {
        dbCallChecker.logDbCall();
    }

    public void reset() {
        dbCallChecker.reset();
    }
}
