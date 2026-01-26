package sky.spring.transaction_lock.event_with_external_update.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import sky.spring.transaction_lock.event_with_external.external.ExternalEventApi;
import sky.spring.transaction_lock.event_with_external.external.dto.ExternalEventResponse;
import sky.spring.transaction_lock.event_with_external.service.EventExternalUpdateService;
import sky.spring.transaction_lock.event_with_lock.entity.EventWithLockParticipant;
import sky.spring.transaction_lock.event_with_lock.repository.EventWithLockParticipantRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExternalApiUpdateRecoveryService {

    private final EventWithLockParticipantRepository participantRepository;
    private final ExternalEventApi externalEventApi;
    private final EventExternalUpdateService eventExternalUpdateService;


    @Scheduled(fixedDelay = 1000 * 60 * 5)
    public void recoverMissingExternalIds() {
        List<EventWithLockParticipant> participantWithoutExternalId
                = participantRepository.findByExternalIdIsNull();

        log.info("외부 ID 미할당 참가자 발견: {}", participantWithoutExternalId.size());

        for (EventWithLockParticipant participant : participantWithoutExternalId) {
            syncExternalId(participant);
        }
    }

    public void syncExternalId(EventWithLockParticipant participant) {
        ExternalEventResponse response = externalEventApi.getParticipantInfo(
                participant.getEvent().getId(),
                participant.getMember().getId()
        );

        if (!response.isSuccess() || response.getExternalId() == null) {
            log.warn("참가자 ID: {}의 외부 시스템 정보 없음 (응답: {})",
                    participant.getId(), response);
            return;
        }

        eventExternalUpdateService.updateExternalId(participant, response.getExternalId());
    }
}
