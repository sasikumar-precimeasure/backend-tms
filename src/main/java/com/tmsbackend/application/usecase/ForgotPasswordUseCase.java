package com.tmsbackend.application.usecase;

import com.tmsbackend.domain.model.MailSenderSettings;
import com.tmsbackend.domain.model.User;
import com.tmsbackend.domain.port.MailSenderPort;
import com.tmsbackend.domain.port.MailSettingsRepositoryPort;
import com.tmsbackend.domain.port.PasswordResetTokenRepositoryPort;
import com.tmsbackend.domain.port.UserRepositoryPort;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ForgotPasswordUseCase {
    public static class SeedAdminForgotPasswordException extends RuntimeException {
        public SeedAdminForgotPasswordException() {
            super("The built-in admin account cannot use Forgot Password. Ask another administrator to reset it from Users, or change it directly after logging in.");
        }
    }

    private final UserRepositoryPort userRepository;
    private final PasswordResetTokenRepositoryPort resetTokenRepository;
    private final RefreshTokenGenerator tokenGenerator;
    private final MailSenderPort mailSender;
    private final MailSettingsRepositoryPort mailSettingsRepository;
    private final String frontendResetUrlBase;
    private final String seedAdminUsername;

    public ForgotPasswordUseCase(
            UserRepositoryPort userRepository,
            PasswordResetTokenRepositoryPort resetTokenRepository,
            RefreshTokenGenerator tokenGenerator,
            MailSenderPort mailSender,
            MailSettingsRepositoryPort mailSettingsRepository,
            @Value("${tms.cors.allowed-origins}") String frontendResetUrlBase,
            @Value("${tms.admin-seed.username}") String seedAdminUsername) {
        this.userRepository = userRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.tokenGenerator = tokenGenerator;
        this.mailSender = mailSender;
        this.mailSettingsRepository = mailSettingsRepository;
        this.frontendResetUrlBase = frontendResetUrlBase;
        this.seedAdminUsername = seedAdminUsername;
    }

    // Always silently succeeds from the caller's point of view for every
    // other account (never reveals whether an email is registered) - except
    // the one seeded bootstrap admin account, which is called out with an
    // explicit error instead. That account is the one guaranteed way back
    // into a fresh install if every other user/role gets misconfigured, so
    // letting Forgot Password silently rotate its password (e.g. via a
    // compromised or misconfigured mail server) would remove that safety
    // net; an admin who legitimately forgot it resets it via another
    // super-admin account, or directly after logging in.
    public void execute(String email) {
        userRepository.findByEmail(email)
                .filter(User::status)
                .ifPresent(user -> {
                    if (user.userName().equals(seedAdminUsername)) {
                        throw new SeedAdminForgotPasswordException();
                    }
                    sendResetEmail(user);
                });
    }

    private void sendResetEmail(User user) {
        String rawToken = tokenGenerator.generate();
        String hash = tokenGenerator.hash(rawToken);
        resetTokenRepository.save(user.id(), hash, Instant.now().plus(Duration.ofHours(1)));

        MailSenderSettings senderSettings = mailSettingsRepository.getSenderSettings();
        if (senderSettings.smtpHost().isBlank()) {
            // No SMTP configured yet for this install - nothing to send;
            // an admin must configure Mail Configuration first.
            return;
        }

        String resetLink = frontendResetUrlBase.split(",")[0].trim() + "/reset-password?token=" + rawToken;
        String body = "<p>A password reset was requested for your account.</p>"
                + "<p><a href=\"" + resetLink + "\">Click here to reset your password</a> (expires in 1 hour).</p>"
                + "<p>If you did not request this, you can ignore this email.</p>";
        mailSender.send(senderSettings, List.of(user.email()), "Password Reset Request", body);
    }
}
