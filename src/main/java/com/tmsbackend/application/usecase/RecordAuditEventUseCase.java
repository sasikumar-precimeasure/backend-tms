package com.tmsbackend.application.usecase;

import com.tmsbackend.application.dto.AuditEventRequest;
import com.tmsbackend.domain.model.AuditEvent;
import com.tmsbackend.domain.port.AuditEventRepositoryPort;
import java.time.Instant;
import org.springframework.stereotype.Component;

// The actor (userId) always comes from the authenticated caller's JWT at
// the controller boundary, never from the request body - so a client can't
// forge "who did it" in the audit trail.
@Component
public class RecordAuditEventUseCase {
    private final AuditEventRepositoryPort auditEventRepository;

    public RecordAuditEventUseCase(AuditEventRepositoryPort auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    public void execute(Long userId, AuditEventRequest request) {
        auditEventRepository.save(new AuditEvent(
                null, Instant.now(), userId, request.deviceId(), request.eventType(),
                request.fieldName(), request.oldValue(), request.newValue(), request.description(),
                request.rawPayloadJson()));
    }
}
