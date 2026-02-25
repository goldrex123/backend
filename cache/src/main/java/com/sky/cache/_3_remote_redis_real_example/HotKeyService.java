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

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class HotKeyService {

    private static final String SERVICE_NAME = "HotKeyService";
    private static final String NULL_VALUE = "__NULL__";

    private static final long TTL_BASE = 500;
    private static final long JITTER_RANGE = 10;

    private final BookRepository bookRepository;
    private final RedisTemplate<String,String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    private final DbCallChecker dbCallChecker = new DbCallChecker("JITTER 적용");

    public Book findBookByIdWithoutLock(Long id) {
        String cachedResult = redisTemplate.opsForValue().get(id.toString());

        if (cachedResult != null && cachedResult.equals(NULL_VALUE)) {
            meterRegistry.counter("cache.hit", "service", SERVICE_NAME).increment();
            return null;
        }

        if (cachedResult != null) {
            meterRegistry.counter("cache.hit", "service", SERVICE_NAME).increment();
            return objectMapper.readValue(cachedResult, new TypeReference<Book>() {
            });
        }

        meterRegistry.counter("cache.miss", "service", SERVICE_NAME).increment();

        Book bookFromDb = bookRepository.findById(id).orElse(null);
        dbCallChecker.incrementDbSelectCount();

        if (bookFromDb == null) {
            long ttl = TTL_BASE + ThreadLocalRandom.current().nextLong(JITTER_RANGE);
            redisTemplate.opsForValue().set(id.toString(), NULL_VALUE, ttl, TimeUnit.MILLISECONDS);
            return bookFromDb;
        }

        String serializedBookFromDb = objectMapper.writeValueAsString(bookFromDb);
        long ttl = TTL_BASE + ThreadLocalRandom.current().nextLong(JITTER_RANGE);
        redisTemplate.opsForValue().set(id.toString(), serializedBookFromDb, ttl, TimeUnit.MILLISECONDS);
        return bookFromDb;
    }

    public void logDbCall() {
        dbCallChecker.logDbCall();
    }

    public void reset() {
        dbCallChecker.reset();
    }
}
