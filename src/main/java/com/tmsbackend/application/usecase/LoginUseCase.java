package com.tmsbackend.application.usecase;

import com.tmsbackend.application.dto.AuthResult;
import com.tmsbackend.domain.model.User;
import com.tmsbackend.domain.port.PasswordHasherPort;
import com.tmsbackend.domain.port.RefreshTokenRepositoryPort;
import com.tmsbackend.domain.port.TokenServicePort;
import com.tmsbackend.domain.port.UserRepositoryPort;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LoginUseCase {
    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException() {
            super("Invalid username or password");
        }
    }

    public static class AccountDisabledException extends RuntimeException {
        public AccountDisabledException() {
            super("This account has been disabled");
        }
    }

    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;
    private final TokenServicePort tokenService;
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final long refreshTokenTtlSeconds;

    public LoginUseCase(
            UserRepositoryPort userRepository,
            PasswordHasherPort passwordHasher,
            TokenServicePort tokenService,
            RefreshTokenRepositoryPort refreshTokenRepository,
            RefreshTokenGenerator refreshTokenGenerator,
            @Value("${tms.jwt.refresh-token-ttl-seconds}") long refreshTokenTtlSeconds) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenService = tokenService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.refreshTokenTtlSeconds = refreshTokenTtlSeconds;
    }

    // Accepts either the username or the email in the same field - the
    // frontend's LoginPage.tsx requires an email-shaped value, but the
    // backend's own User model treats userName and email as independent
    // fields, so a plain username alone wouldn't satisfy that form. Trying
    // username first (the more common case, and what the field is named)
    // then falling back to email keeps both entry points working without
    // the frontend needing changes.
    public AuthResult execute(String usernameOrEmail, String password) {
        User user = userRepository
                .findByUserName(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordHasher.matches(password, user.passwordHash())) {
            throw new InvalidCredentialsException();
        }
        if (!user.status()) {
            throw new AccountDisabledException();
        }

        userRepository.updateLastLoginAt(user.id(), Instant.now());

        TokenServicePort.IssuedAccessToken accessToken = tokenService.issueAccessToken(user);

        String refreshToken = refreshTokenGenerator.generate();
        String refreshTokenHash = refreshTokenGenerator.hash(refreshToken);
        refreshTokenRepository.save(user.id(), refreshTokenHash, Instant.now().plus(Duration.ofSeconds(refreshTokenTtlSeconds)));

        return new AuthResult(accessToken.token(), refreshToken, accessToken.expiresInSeconds(), user);
    }
}
