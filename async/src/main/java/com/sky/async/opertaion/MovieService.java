package com.sky.async.opertaion;

import com.sky.async.opertaion.client.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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
        Movie movie = movieRepository.findById(movieId).orElseThrow();

        int ranking = rankingClient.getRanking(movieId);
        boolean isAvaliable = bookingClient.isAvailable(movieId);
        List<Long> recommendedMovieIds = recommendClient.getRecommendedMovieIds(userNo);

        interestEmitter.emitUserInterest(userNo, movieId);

        return MovieDetailResult.builder()
                .movieId(movie.getId())
                .title(movie.getTitle())
                .ranking(ranking)
                .isAvailable(isAvaliable)
                .recommendedMovieIds(recommendedMovieIds)
                .build();
    }

    public void sendBookingConfirmationPush(Map<Long, Long> bookingIdsByUserNo) {
        bookingIdsByUserNo.forEach((userNo, bookingId) -> {
            moviePushNotificationClient.sendBookingConfirmation(userNo, bookingId);
        });
    }

}
