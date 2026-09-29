package com.tmsbackend.application.usecase;

import com.tmsbackend.domain.port.RefreshTokenRepositoryPort;
import org.springframework.stereotype.Component;

@Component
public class LogoutUseCase {
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final RefreshTokenGenerator refreshTokenGenerator;

    public LogoutUseCase(RefreshTokenRepositoryPort refreshTokenRepository, RefreshTokenGenerator refreshTokenGenerator) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenGenerator = refreshTokenGenerator;
    }

    public void execute(String presentedRefreshToken) {
        if (presentedRefreshToken == null || presentedRefreshToken.isBlank()) {
            return;
        }
        String hash = refreshTokenGenerator.hash(presentedRefreshToken);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(stored -> refreshTokenRepository.revoke(stored.id()));
    }
}
