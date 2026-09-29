package com.tmsbackend.domain.model;

import java.time.Instant;

// The "who did what" record - user comes from the authenticated caller's
// JWT at the controller boundary, never from the request body itself, so it
// can't be spoofed by the frontend.
public record AuditEvent(
        Long id,
        Instant occurredAt,
        Long userId,
        String deviceId,
        AuditEventType eventType,
        String fieldName,
        String oldValue,
        String newValue,
        String description,
        String rawPayloadJson) {
}
