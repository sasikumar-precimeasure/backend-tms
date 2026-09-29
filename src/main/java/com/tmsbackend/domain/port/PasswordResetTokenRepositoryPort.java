package com.tmsbackend.domain.port;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepositoryPort {
    record StoredResetToken(Long id, Long userId, String tokenHash, Instant expiresAt, boolean used) {
    }

    StoredResetToken save(Long userId, String tokenHash, Instant expiresAt);

    Optional<StoredResetToken> findByTokenHash(String tokenHash);

    void markUsed(Long id);
}
