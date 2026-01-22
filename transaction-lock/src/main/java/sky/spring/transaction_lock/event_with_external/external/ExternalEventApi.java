package sky.spring.transaction_lock.event_with_external.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sky.spring.transaction_lock.event_with_external.external.dto.ExternalEventResponse;

import java.util.Random;
import java.util.UUID;

@Component
@Slf4j
public class ExternalEventApi {

    private static final Random RANDOM = new Random();

    public ExternalEventResponse registerParticipant(Long eventId, Long memberId, String eventName) {
        try {
            Thread.sleep(RANDOM.nextInt(500, 1500));
            return ExternalEventResponse.builder()
                    .success(true)
                    .externalId(UUID.randomUUID().toString())
                    .errorMessage(null)
                    .build();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public ExternalEventResponse getParticipantInfo(Long eventId, Long memberId) {
        try {
            Thread.sleep(100);
            return ExternalEventResponse.builder()
                    .success(true)
                    .externalId(UUID.randomUUID().toString())
                    .errorMessage(null)
                    .build();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
