package com.sky.cache._3_remote_redis_real_example.improved;

import com.sky.cache._1_practice.Book;
import com.sky.cache._1_practice.BookRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class ImprovedBookRedisService {

    private final BookRepository bookRepository;
    private final RedisTemplate<String,String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    private static final String NULL_VALUE = "__NULL__";

    public Book findBookByIdWithNullCache(Long id) {
        String cachedResult = redisTemplate.opsForValue().get(id.toString());

        if (cachedResult != null && cachedResult.equals(NULL_VALUE)) {
            meterRegistry.counter("cache.hit", "service", "ImprovedBookRedisService").increment();
            return null;
        }

        if (cachedResult != null) {
            meterRegistry.counter("cache.hit", "service", "ImprovedBookRedisService").increment();
            return objectMapper.readValue(cachedResult, new TypeReference<Book>() {
            });
        }

        meterRegistry.counter("cache.miss", "service", "ImprovedBookRedisService").increment();

        Book bookFromDb = bookRepository.findById(id).orElse(null);
        meterRegistry.counter("db.select", "service", "ImprovedBookRedisService").increment();

        if (bookFromDb == null) {
            redisTemplate.opsForValue().set(id.toString(), NULL_VALUE, 5, TimeUnit.MINUTES);
            return bookFromDb;
        }

        String serializedBookFromDb = objectMapper.writeValueAsString(bookFromDb);
        redisTemplate.opsForValue().set(id.toString(), serializedBookFromDb, 30, TimeUnit.MINUTES);
        return bookFromDb;
    }
}
