package com.sky.async.opertaion.improve_client;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class MovieImprovedPushNotificationClient {

    @Async("asyncExecutor")
    public CompletableFuture<Void> sendBookingConfirmation(Long userNo, Long bookingId) {
        try {
            Thread.sleep(200L);
        } catch (InterruptedException e) {
        }
        return CompletableFuture.completedFuture(null);
    }
}
