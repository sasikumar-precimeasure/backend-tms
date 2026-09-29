package com.tmsbackend.infrastructure.mail;

import com.tmsbackend.domain.model.MailSenderSettings;
import com.tmsbackend.domain.port.MailSenderPort;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import java.util.Properties;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

// SMTP host/port/credentials come from the database (mail_sender_settings,
// editable via Mail Configuration), not static application.yaml config -
// a JavaMailSenderImpl is built fresh per send so editing settings takes
// effect on the next scheduled tick without an app restart.
@Component
public class JavaMailSenderAdapter implements MailSenderPort {
    @Override
    public void send(MailSenderSettings senderSettings, List<String> toAddresses, String subject, String htmlBody) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(senderSettings.smtpHost());
        mailSender.setPort(senderSettings.smtpPort());
        mailSender.setUsername(senderSettings.senderEmail());
        mailSender.setPassword(senderSettings.password());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", String.valueOf(senderSettings.enableSsl()));
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.connectiontimeout", "10000");

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(senderSettings.senderEmail(), senderSettings.senderName());
            helper.setTo(toAddresses.toArray(new String[0]));
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to send mail: " + e.getMessage(), e);
        }
    }
}
