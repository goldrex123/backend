package com.sky.async.opertaion.client;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class MovieInterestEmitter {

    @Async("asyncExecutor")
    public void emitUserInterest(Long userNo, Long movieId) {
        try {
            Thread.sleep(200L);
        } catch (InterruptedException e) {
        }
    }
}
