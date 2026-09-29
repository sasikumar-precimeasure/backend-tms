package com.tmsbackend.domain.model;

// Mirrors tms/src/domain/entities/MailSettings.ts's MailSenderSettings - one
// app-wide row. `password` here is always the plaintext value in memory;
// encryption at rest happens only in the persistence adapter, so the
// domain/application layers never need to know an encryption scheme exists.
public record MailSenderSettings(
        String senderName, String senderEmail, String smtpHost, int smtpPort, String password, boolean enableSsl) {
}
