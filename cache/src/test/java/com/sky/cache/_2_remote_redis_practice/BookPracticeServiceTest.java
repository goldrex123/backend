package com.sky.cache._2_remote_redis_practice;

import com.sky.cache._1_practice.Book;
import com.sky.cache._1_practice.BookRepository;
import org.assertj.core.api.Assertions;
import org.hibernate.type.descriptor.java.BooleanPrimitiveArrayJavaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BookPracticeServiceTest {

    @Autowired
    private BookPracticeService bookPracticeService;

    @Autowired
    private BookRepository bookRepository;

    private Book book;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAllInBatch();

        Book newBook = Book.builder()
                .name("test")
                .soldOut(false)
                .build();

        book = bookRepository.save(newBook);
    }

    @Test
    void findBookById() {
        Book firstBook = bookPracticeService.findBookById(book.getId());

        System.out.println("--------------------------");

        Book secondBook = bookPracticeService.findBookById(book.getId());

        assertThat(firstBook.getId()).isEqualTo(secondBook.getId());
        assertThat(firstBook.getName()).isEqualTo(secondBook.getName());
    }
}