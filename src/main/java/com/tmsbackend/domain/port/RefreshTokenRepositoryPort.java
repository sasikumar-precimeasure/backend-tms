package com.tmsbackend.domain.port;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepositoryPort {
    record StoredToken(Long id, Long userId, String tokenHash, Instant expiresAt, boolean revoked) {
    }

    StoredToken save(Long userId, String tokenHash, Instant expiresAt);

    Optional<StoredToken> findByTokenHash(String tokenHash);

    void revoke(Long tokenId);

    void revokeAllForUser(Long userId);
}
