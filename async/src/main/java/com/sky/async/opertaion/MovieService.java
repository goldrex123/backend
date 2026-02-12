package com.sky.async.opertaion;

import com.sky.async.opertaion.client.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final MovieBookingClient bookingClient;
    private final MovieRecommendClient recommendClient;
    private final MovieRankingClient rankingClient;
    private final MovieInterestEmitter interestEmitter;
    private final MoviePushNotificationClient moviePushNotificationClient;

    public MovieDetailResult getMovieDetail(Long userNo, Long movieId) {
        


    }

}
