package com.sky.async.opertaion;

import com.sky.async.opertaion.client.MovieDetailResult;
import com.sky.async.opertaion.client.MovieInterestEmitter;
import com.sky.async.opertaion.improve_client.MovieImprovedBookingClient;
import com.sky.async.opertaion.improve_client.MovieImprovedPushNotificationClient;
import com.sky.async.opertaion.improve_client.MovieImprovedRankingClient;
import com.sky.async.opertaion.improve_client.MovieImprovedRecommendClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieImprovedService {

    private final MovieRepository movieRepository;
    private final MovieImprovedBookingClient bookingClient;
    private final MovieImprovedRecommendClient recommendClient;
    private final MovieImprovedRankingClient rankingClient;
    private final MovieInterestEmitter interestEmitter;
    private final MovieImprovedPushNotificationClient movieImprovedPushNotificationClient;

    public MovieDetailResult getMovieDetail(Long userNo, Long movieId) {
        Movie movie = movieRepository.findById(movieId).orElseThrow();

        CompletableFuture<Integer> ranking = rankingClient.getRanking(movieId);
        CompletableFuture<Boolean> available = bookingClient.isAvailable(movieId);
        CompletableFuture<List<Long>> recommendedMovieIds = recommendClient.getRecommendedMovieIds(userNo);

        interestEmitter.emitUserInterest(userNo,movieId);

        return MovieDetailResult.builder()
                .movieId(movie.getId())
                .title(movie.getTitle())
                .ranking(ranking.join())
                .isAvailable(available.join())
                .recommendedMovieIds(recommendedMovieIds.join())
                .build();
    }


    public void sendBookingConfirmationPush(Map<Long, Long> bookingIdByUserNo) {

        List<CompletableFuture<Void>> futures = bookingIdByUserNo.entrySet()
                .stream()
                .map(entry -> movieImprovedPushNotificationClient.sendBookingConfirmation(entry.getKey(), entry.getValue())
                        .exceptionally(ex -> {
                            log.warn("푸시 알림 실패", ex);
                            return null;
                        }))
                .collect(Collectors.toList());

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
    }

}
