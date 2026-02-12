package com.sky.async.opertaion.client;

import org.springframework.stereotype.Component;

@Component
public class MoviePushNotificationClient {
    public void sendBookingConfirmation(Long userNo, Long bookingId) {
        try {
            Thread.sleep(200L);
        } catch (InterruptedException e) {
        }
    }
}
