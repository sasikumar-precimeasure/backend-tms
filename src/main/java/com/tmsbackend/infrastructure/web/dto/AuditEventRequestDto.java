package com.tmsbackend.infrastructure.web.dto;

import com.tmsbackend.application.dto.AuditEventRequest;
import com.tmsbackend.domain.model.AuditEventType;
import jakarta.validation.constraints.NotNull;

// The actor never travels in this body - it's read from the caller's JWT at
// the controller, so a client can't forge "who did it".
public record AuditEventRequestDto(
        @NotNull AuditEventType eventType, String deviceId, String fieldName, String oldValue, String newValue,
        String description, String rawPayloadJson) {

    public AuditEventRequest toDomain() {
        return new AuditEventRequest(eventType, deviceId, fieldName, oldValue, newValue, description, rawPayloadJson);
    }
}
