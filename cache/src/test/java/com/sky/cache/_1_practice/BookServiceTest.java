package com.sky.cache._1_practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BookServiceTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CacheManager cacheManager;

    private Book book = null;

    @BeforeEach
    void setUp() {
        Book book = Book.builder()
                .name("테스트")
                .isSoldOut(false)
                .build();

        bookRepository.save(book);
        this.book = book;
    }

    @AfterEach
    void clean() {
        bookRepository.deleteAllInBatch();
        Collection<String> cacheNames = cacheManager.getCacheNames();
        for (String cacheName : cacheNames) {
            cacheManager.getCache(cacheName).invalidate();
        }
    }

    @DisplayName("캐시 적용 테스트")
    @Test
    void cacheTest() {
        Long bookId = book.getId();

        bookService.findBookById(bookId);
        bookService.findBookById(book.getId());
    }
}