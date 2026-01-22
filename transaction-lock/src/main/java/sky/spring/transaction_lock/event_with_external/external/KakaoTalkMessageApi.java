package sky.spring.transaction_lock.event_with_external.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@Slf4j
public class KakaoTalkMessageApi {

    private static final Random RANDOM = new Random();

    public void sendEventJoinMessage(String phoneNumber, String eventName) {
        try {
            Thread.sleep(RANDOM.nextInt(500, 1500));
            log.info("카카오톡 알림 발송 완료 - 이벤트: {}", eventName);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 상위 스레드로 인터럽트 정보 전달
            throw new RuntimeException("카카오톡 알림 발송 중 인터럽트 발생",e);
        }
    }
}
