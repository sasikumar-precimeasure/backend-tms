package com.tmsbackend.application.usecase;

import com.tmsbackend.application.dto.AuthResult;
import com.tmsbackend.domain.model.User;
import com.tmsbackend.domain.port.RefreshTokenRepositoryPort;
import com.tmsbackend.domain.port.RefreshTokenRepositoryPort.StoredToken;
import com.tmsbackend.domain.port.TokenServicePort;
import com.tmsbackend.domain.port.UserRepositoryPort;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// Rotates the refresh token on every use (old one revoked, a new one
// issued) - matches the frontend's client.ts interceptor, which always
// stores whatever refreshToken comes back from /auth/refresh.
@Component
public class RefreshTokenUseCase {
    public static class InvalidRefreshTokenException extends RuntimeException {
        public InvalidRefreshTokenException() {
            super("Invalid or expired refresh token");
        }
    }

    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final UserRepositoryPort userRepository;
    private final TokenServicePort tokenService;
    private final long refreshTokenTtlSeconds;

    public RefreshTokenUseCase(
            RefreshTokenRepositoryPort refreshTokenRepository,
            RefreshTokenGenerator refreshTokenGenerator,
            UserRepositoryPort userRepository,
            TokenServicePort tokenService,
            @Value("${tms.jwt.refresh-token-ttl-seconds}") long refreshTokenTtlSeconds) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.refreshTokenTtlSeconds = refreshTokenTtlSeconds;
    }

    public AuthResult execute(String presentedRefreshToken) {
        String presentedHash = refreshTokenGenerator.hash(presentedRefreshToken);
        StoredToken stored = refreshTokenRepository
                .findByTokenHash(presentedHash)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (stored.revoked() || stored.expiresAt().isBefore(Instant.now())) {
            throw new InvalidRefreshTokenException();
        }

        User user = userRepository.findById(stored.userId()).orElseThrow(InvalidRefreshTokenException::new);
        if (!user.status()) {
            throw new InvalidRefreshTokenException();
        }

        refreshTokenRepository.revoke(stored.id());

        String newRefreshToken = refreshTokenGenerator.generate();
        String newHash = refreshTokenGenerator.hash(newRefreshToken);
        refreshTokenRepository.save(user.id(), newHash, Instant.now().plus(Duration.ofSeconds(refreshTokenTtlSeconds)));

        TokenServicePort.IssuedAccessToken accessToken = tokenService.issueAccessToken(user);
        return new AuthResult(accessToken.token(), newRefreshToken, accessToken.expiresInSeconds(), user);
    }
}
