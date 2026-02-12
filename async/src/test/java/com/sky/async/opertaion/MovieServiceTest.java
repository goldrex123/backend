package com.sky.async.opertaion;

import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.concurrent.TimeUnit;


@Slf4j
@SpringBootTest
class MovieServiceTest {

    private static final int REPEAT_COUNT = 10;
    private static final int WARM_UP_COUNT = 5;
    private static final Long USER_NO = 1L;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private MovieService movieService;

    @Autowired
    private MeterRegistry meterRegistry;

    @DisplayName("영화 상세 조회 - 동기 실행")
    @RepeatedTest(REPEAT_COUNT)
    void testSyncGetMovieDetail(RepetitionInfo repetitionInfo) {
        Timer timer = meterRegistry.timer("sync-get-movie-detail");
        Movie movie = movieRepository.save(new Movie("test"));
        Long movieId = movie.getId();

        if (repetitionInfo.getCurrentRepetition() <= WARM_UP_COUNT) {
            movieService.getMovieDetail(USER_NO, movieId);
        } else {
            timer.record(() -> movieService.getMovieDetail(USER_NO, movieId));
        }

        if (repetitionInfo.getCurrentRepetition() == REPEAT_COUNT) {
            double mean = timer.mean(TimeUnit.MILLISECONDS);
            log.info("Sync movie detail timer - mean={}ms", String.format("%.2f", mean));
        }
    }
}