package com.sky.cache._3_remote_redis_real_example;

import com.sky.cache._1_practice.Book;
import com.sky.cache._1_practice.BookRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class CacheAvalancheService {

    private static final String SERVICE_NAME = "CacheAvalancheService";
    private static final String NULL_VALUE = "__NULL__";

    private static final long TTL_BASE = 1000;

    private final BookRepository bookRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    private final DbCallChecker dbCallChecker = new DbCallChecker("모두 같은 TTL");

    public Book findBookByIdWithNoJitter(Long id) {
        String cachedResult = redisTemplate.opsForValue().get(id.toString());

        // 2. 캐시에 존재하지만 NULL_VALUE가 저장되어 있다면 (데이터가 없음을 의미)
        if (cachedResult != null && cachedResult.equals(NULL_VALUE)) {
            meterRegistry.counter("cache.hit", "service", SERVICE_NAME).increment();
            return null;
        }

        // 3. 캐시에 존재하지만 NULL_VALUE 는 아닌 경우
        if (cachedResult != null) {
            meterRegistry.counter("cache.hit", "service", SERVICE_NAME).increment();
            return objectMapper.readValue(cachedResult, new TypeReference<>() {
            });
        }

        meterRegistry.counter("cache.miss", "service", SERVICE_NAME).increment();

        // 3. 캐시에 아예 없는 경우 DB에서 조회
        Book bookFromDb = bookRepository.findById(id).orElse(null);
        dbCallChecker.incrementDbSelectCount();

        // 4. DB 조회 결과가 null이더라도 캐시에 저장
        if (bookFromDb == null) {
            redisTemplate.opsForValue().set(id.toString(), NULL_VALUE, TTL_BASE, TimeUnit.MILLISECONDS);
            return bookFromDb;
        }

        String serializedBookFromDb = objectMapper.writeValueAsString(bookFromDb);
        redisTemplate.opsForValue().set(id.toString(), serializedBookFromDb, TTL_BASE, TimeUnit.MILLISECONDS);
        return bookFromDb;
    }


    public void logDbCall() {
        dbCallChecker.logDbCall();
    }

    public void reset() {
        dbCallChecker.reset();
    }
}
