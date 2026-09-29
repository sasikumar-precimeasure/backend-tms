package com.tmsbackend.application.usecase;

import com.tmsbackend.domain.model.User;
import com.tmsbackend.domain.port.PasswordHasherPort;
import com.tmsbackend.domain.port.PasswordResetTokenRepositoryPort;
import com.tmsbackend.domain.port.PasswordResetTokenRepositoryPort.StoredResetToken;
import com.tmsbackend.domain.port.RefreshTokenRepositoryPort;
import com.tmsbackend.domain.port.UserRepositoryPort;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class ResetPasswordUseCase {
    public static class InvalidResetTokenException extends RuntimeException {
        public InvalidResetTokenException() {
            super("Invalid or expired reset token");
        }
    }

    public static class PasswordMismatchException extends RuntimeException {
        public PasswordMismatchException() {
            super("Passwords do not match");
        }
    }

    private final PasswordResetTokenRepositoryPort resetTokenRepository;
    private final RefreshTokenGenerator tokenGenerator;
    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;
    private final RefreshTokenRepositoryPort refreshTokenRepository;

    public ResetPasswordUseCase(
            PasswordResetTokenRepositoryPort resetTokenRepository,
            RefreshTokenGenerator tokenGenerator,
            UserRepositoryPort userRepository,
            PasswordHasherPort passwordHasher,
            RefreshTokenRepositoryPort refreshTokenRepository) {
        this.resetTokenRepository = resetTokenRepository;
        this.tokenGenerator = tokenGenerator;
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public void execute(String token, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new PasswordMismatchException();
        }

        String hash = tokenGenerator.hash(token);
        StoredResetToken stored = resetTokenRepository.findByTokenHash(hash).orElseThrow(InvalidResetTokenException::new);
        if (stored.used() || stored.expiresAt().isBefore(Instant.now())) {
            throw new InvalidResetTokenException();
        }

        User user = userRepository.findById(stored.userId()).orElseThrow(InvalidResetTokenException::new);
        User updated = new User(
                user.id(), user.userName(), user.fullName(), user.email(), user.mobile(),
                passwordHasher.hash(newPassword), user.status(), user.createdDate(), user.lastLoginAt(),
                user.roleId(), user.role(), user.permissions(), user.imgUrl(), user.timezone(), user.dateFormat());
        userRepository.save(updated);

        resetTokenRepository.markUsed(stored.id());
        // A password reset invalidates every existing session - force
        // re-login everywhere, matching standard security practice.
        refreshTokenRepository.revokeAllForUser(user.id());
    }
}
