package com.tmsbackend.application.dto;

import com.tmsbackend.domain.model.AuditEventType;

public record AuditEventRequest(
        AuditEventType eventType, String deviceId, String fieldName, String oldValue, String newValue,
        String description, String rawPayloadJson) {
}
