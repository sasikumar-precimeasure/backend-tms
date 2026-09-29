package com.tmsbackend.infrastructure.web.dto;

import com.tmsbackend.domain.model.AuditEvent;
import com.tmsbackend.domain.model.AuditEventType;
import java.time.Instant;

public record AuditEventDto(
        Long id, Instant occurredAt, Long userId, String deviceId, AuditEventType eventType, String fieldName,
        String oldValue, String newValue, String description) {

    public static AuditEventDto from(AuditEvent event) {
        return new AuditEventDto(
                event.id(), event.occurredAt(), event.userId(), event.deviceId(), event.eventType(),
                event.fieldName(), event.oldValue(), event.newValue(), event.description());
    }
}
