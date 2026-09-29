package com.tmsbackend.infrastructure.web.dto;

import com.tmsbackend.domain.model.MailSenderSettings;
import jakarta.validation.constraints.NotBlank;

// Matches tms/src/domain/entities/MailSettings.ts's MailSenderSettings.
// password is write-only from the frontend's point of view: it's never
// echoed back with the real value (see MailSettingsController.getSender),
// and an empty string on save means "keep the existing password".
public record MailSenderSettingsDto(
        @NotBlank String senderName, @NotBlank String senderEmail, @NotBlank String smtpHost, int smtpPort,
        String password, boolean enableSsl) {

    public MailSenderSettings toDomain() {
        return new MailSenderSettings(senderName, senderEmail, smtpHost, smtpPort, password, enableSsl);
    }

    public static MailSenderSettingsDto from(MailSenderSettings settings) {
        // Never echo the real password back to the client.
        return new MailSenderSettingsDto(
                settings.senderName(), settings.senderEmail(), settings.smtpHost(), settings.smtpPort(), "", settings.enableSsl());
    }
}
