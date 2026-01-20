package sky.spring.transaction_lock.event;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sky.spring.transaction_lock.event.entity.Event;
import sky.spring.transaction_lock.event.fixture.EventFixture;
import sky.spring.transaction_lock.event.repository.EventRepository;
import sky.spring.transaction_lock.event.service.Event1Service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
@SpringBootTest
class Event1ServiceTest {

    @Autowired
    private Event1Service event1Service;

    @Autowired
    private EventRepository eventRepository;

    private Event testEvent;


    @BeforeEach
    void setUp() {
        testEvent = eventRepository.save(
                EventFixture.createEvent("이벤트 테스트", 100)
        );
    }

    @Test
    @DisplayName("단일 스레드에서 트랜잭션 정상 처리")
    void transactionWorksInSingleThread() {
        int callNumber = 150;
        int successCount = 0;

        for (int i = 0; i < callNumber; i++) {
            try {
                event1Service.increaseParticipant(testEvent.getId());
                successCount++;
            } catch (Exception e) {
                log.info("이벤트 참가 에러 발생  - {}", e.getMessage());
            }
        }

        Event updatedEvent = eventRepository.findById(testEvent.getId()).orElseThrow();

        System.out.println("이벤트 최대 참가 가능 인원: " + updatedEvent.getMaxParticipants());
        System.out.println("호출 성공 회수: " + successCount);
        System.out.println("DB에 저장된 참가자 수: " + updatedEvent.getCurrentParticipants());

        assertThat(updatedEvent.getCurrentParticipants()).isEqualTo(successCount);
        assertThat(updatedEvent.getCurrentParticipants()).isLessThanOrEqualTo(updatedEvent.getMaxParticipants());
    }

    @Test
    @DisplayName("트랜잭션 만으로는 동시성 제어가 되지 않음")
    void transactionDoesNotControlConcurrency() throws InterruptedException {
        int threadNumber = 100;
        AtomicInteger successCount = new AtomicInteger(0);
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadNumber);

        for (int i = 0; i < threadNumber; i++) {
            executorService.submit(() -> {
                try {
                    event1Service.increaseParticipant(testEvent.getId());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("이벤트 참가 실패 - {}", e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdownNow();

        Event updatedEvent = eventRepository.findById(testEvent.getId()).orElseThrow();

        System.out.println("이벤트 최대 참가 가능 인원: " + updatedEvent.getMaxParticipants());
        System.out.println("호출 성공 회수: " + successCount.get());
        System.out.println("DB에 저장된 참가자 수: " + updatedEvent.getCurrentParticipants());

        assertThat(updatedEvent.getCurrentParticipants()).isNotEqualTo(successCount.get());
        assertThat(updatedEvent.getCurrentParticipants()).isLessThan(successCount.get());
    }
}