package com.sky.async.opertaion.client;

import org.springframework.stereotype.Component;

@Component
public class MovieInterestEmitter {

    public void emitUserInterest(Long userNo, Long movieId) {
        try {
            Thread.sleep(200L);
        } catch (InterruptedException e) {
        }
    }
}
