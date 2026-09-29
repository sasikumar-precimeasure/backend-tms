package com.tmsbackend.domain.model;

import java.util.List;

// Mirrors tms/src/domain/entities/MailSettings.ts's MailRecipient -
// deviceIds is the per-device opt-in list (a deliberate refinement of the
// legacy VB app's coarser per-TR checkboxes).
public record MailRecipient(String id, String name, String email, boolean enabled, List<String> deviceIds) {
}
