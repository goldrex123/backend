package sky.spring.transaction_lock.event_with_external.external.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExternalEventResponse {

    private boolean success;
    private String externalId;
    private String message;
    private String errorMessage;
}
