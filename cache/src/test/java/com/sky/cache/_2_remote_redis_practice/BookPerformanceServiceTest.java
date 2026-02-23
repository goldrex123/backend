package com.sky.cache._2_remote_redis_practice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class BookPerformanceServiceTest extends AbstractBookTest {

    @Autowired
    private BookPerformanceService bookPerformanceService;

    @DisplayName("DB 조회")
    @RepeatedTest(REPEATED_COUNT)
    void findBookByIdWitoutCache(RepetitionInfo repetitionInfo) {
        measureAndRecordTime("DB 조회", repetitionInfo.getCurrentRepetition(), () -> {
            bookPerformanceService.findBookByIdWithoutCache();
        });
    }

    @DisplayName("Redis 캐시 조회")
    @RepeatedTest(REPEATED_COUNT)
    void findBookByIdWithCache(RepetitionInfo repetitionInfo) {
        measureAndRecordTime("Redis 조회", repetitionInfo.getCurrentRepetition(), () -> {
            bookPerformanceService.findBookByIdWithCache();
        });
    }


}