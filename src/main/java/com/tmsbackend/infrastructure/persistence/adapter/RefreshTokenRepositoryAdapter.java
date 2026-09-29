package com.tmsbackend.infrastructure.persistence.adapter;

import com.tmsbackend.domain.port.RefreshTokenRepositoryPort;
import com.tmsbackend.infrastructure.persistence.entity.RefreshTokenEntity;
import com.tmsbackend.infrastructure.persistence.repository.RefreshTokenJpaRepository;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepositoryPort {
    private final RefreshTokenJpaRepository jpaRepository;

    public RefreshTokenRepositoryAdapter(RefreshTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public StoredToken save(Long userId, String tokenHash, Instant expiresAt) {
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setUserId(userId);
        entity.setTokenHash(tokenHash);
        entity.setExpiresAt(expiresAt);
        entity.setRevoked(false);
        RefreshTokenEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<StoredToken> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash).map(this::toDomain);
    }

    @Override
    public void revoke(Long tokenId) {
        jpaRepository.findById(tokenId).ifPresent(entity -> {
            entity.setRevoked(true);
            jpaRepository.save(entity);
        });
    }

    @Override
    public void revokeAllForUser(Long userId) {
        jpaRepository.findByUserIdAndRevokedFalse(userId).forEach(entity -> {
            entity.setRevoked(true);
            jpaRepository.save(entity);
        });
    }

    private StoredToken toDomain(RefreshTokenEntity entity) {
        return new StoredToken(entity.getId(), entity.getUserId(), entity.getTokenHash(), entity.getExpiresAt(), entity.isRevoked());
    }
}
