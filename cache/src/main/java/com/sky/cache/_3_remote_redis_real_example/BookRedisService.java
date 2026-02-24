package com.sky.cache._3_remote_redis_real_example;

import com.sky.cache._1_practice.Book;
import com.sky.cache._1_practice.BookRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class BookRedisService {

    private final BookRepository bookRepository;
    private final RedisTemplate<String,String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    public Book findBookById(Long id) {
        String cachedResult = redisTemplate.opsForValue().get(id.toString());

        if (cachedResult != null) {
            meterRegistry.counter("cache.hit", "service", "BookRedisService").increment();
            return objectMapper.readValue(cachedResult, new TypeReference<Book>() {
            });
        }

        meterRegistry.counter("cache.miss", "service", "BookRedisService").increment();

        Book bookFromDb = bookRepository.findById(id).orElse(null);
        meterRegistry.counter("db.select", "service", "BookRedisService").increment();

        if (bookFromDb != null) {
            String serializedBookFormDb = objectMapper.writeValueAsString(bookFromDb);
            redisTemplate.opsForValue().set(id.toString(), serializedBookFormDb, 1, TimeUnit.HOURS);
        }

        return bookFromDb;
    }
}
