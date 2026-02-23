package com.sky.cache._2_remote_redis_practice;

import com.sky.cache._1_practice.Book;
import com.sky.cache._1_practice.BookRepository;
import com.sky.cache.util.TestLogUtil;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractBookTest {

    public static final int REPEATED_COUNT = 20;
    public static final int WARM_UP_COUNT = 10;

    public static final String INSERT_INTO_BOOK_VALUES = """
            INSERT INTO book (name, sold_out)
            VALUES(?,?)        
            """;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private MeterRegistry meterRegistry;

    @BeforeAll
    void setUp() {
        TestLogUtil.setUpStart();
        bookRepository.deleteAllInBatch();

        List<Book> randomBooks = BookDataProvider.createRandomBooks(100_000);

        jdbcTemplate.batchUpdate(INSERT_INTO_BOOK_VALUES, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Book book = randomBooks.get(i);
                ps.setString(1, book.getName());
                ps.setBoolean(2, book.isSoldOut());
            }

            @Override
            public int getBatchSize() {
                return randomBooks.size();
            }
        });

        TestLogUtil.setUpEnd();
    }

    protected void measureAndRecordTime(String operationType, int currentCount, Runnable runnable) {
        Timer timer = meterRegistry.timer(operationType);

        if (currentCount <= WARM_UP_COUNT) {
            runnable.run();
        } else {
            timer.record(runnable);
        }

        if (currentCount == REPEATED_COUNT) {
            double mean = timer.mean(TimeUnit.MILLISECONDS);
            log.info(">>> {} - 평균 소요 시간 (warm-up 제외) = {}ms", operationType, String.format("%.2f", mean));
        }
    }
}
