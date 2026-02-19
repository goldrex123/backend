package com.sky.cache._1_practice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final BookRepository bookRepository;

    @Cacheable(value = "book")
    public Book findBookById(Long id) {
        log.info("findBookById가 실행됩니다");
        return bookRepository.findById(id).orElseThrow();
    }
}
