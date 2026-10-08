package com.tmsbackend.domain.port;

import com.tmsbackend.domain.model.MailAttachment;
import com.tmsbackend.domain.model.MailSenderSettings;
import java.util.List;

public interface MailSenderPort {
    void send(MailSenderSettings senderSettings, List<String> toAddresses, String subject, String htmlBody);

    // A default (not a second abstract method) so this stays a functional
    // interface that tests can fake with a lambda - JavaMailSenderAdapter
    // overrides it with real attachment support.
    default void send(
            MailSenderSettings senderSettings,
            List<String> toAddresses,
            String subject,
            String htmlBody,
            List<MailAttachment> attachments) {
        if (!attachments.isEmpty()) {
            throw new UnsupportedOperationException("This mail sender does not support attachments");
        }
        send(senderSettings, toAddresses, subject, htmlBody);
    }
}
