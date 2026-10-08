package com.tmsbackend.domain.model;

// A monthly report recipient - independent of MailRecipient (alarm emails).
public record ReportRecipient(String id, String name, String email, boolean enabled) {
}
