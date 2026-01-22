package sky.spring.transaction_lock.event_with_external.external.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExternalEventRequest {

    private Long eventId;
    private Long memberId;
    private String eventName;

}
