package com.sky.cache._2_remote_redis_practice;

import com.sky.cache._1_practice.Book;
import com.sky.cache._1_practice.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookPerformanceService {

    private final BookRepository bookRepository;
    private final RedisTemplate<String,String> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String CACHE_KEY = "remote-cache-practice";

    public List<Book> findBookByIdWithoutCache() {
        List<Book> books = bookRepository.findByNameContaining("a");
        return books;
    }

    public List<Book> findBookByIdWithCache() {
        String cacheResult = redisTemplate.opsForValue().get(CACHE_KEY);

        if (cacheResult != null) {
            return objectMapper.readValue(cacheResult, new TypeReference<List<Book>>() {
            });
        }

        List<Book> books = bookRepository.findByNameContaining("a");
        String serializedNew = objectMapper.writeValueAsString(books);

        redisTemplate.opsForValue().set(CACHE_KEY, serializedNew);

        return books;
    }
}
