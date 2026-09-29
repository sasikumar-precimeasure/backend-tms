package com.tmsbackend.domain.port;

import com.tmsbackend.domain.model.MailSenderSettings;
import java.util.List;

public interface MailSenderPort {
    void send(MailSenderSettings senderSettings, List<String> toAddresses, String subject, String htmlBody);
}
