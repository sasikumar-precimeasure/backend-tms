package com.tmsbackend.infrastructure.persistence.adapter;

import com.tmsbackend.domain.port.PasswordResetTokenRepositoryPort;
import com.tmsbackend.infrastructure.persistence.entity.PasswordResetTokenEntity;
import com.tmsbackend.infrastructure.persistence.repository.PasswordResetTokenJpaRepository;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetTokenRepositoryAdapter implements PasswordResetTokenRepositoryPort {
    private final PasswordResetTokenJpaRepository jpaRepository;

    public PasswordResetTokenRepositoryAdapter(PasswordResetTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public StoredResetToken save(Long userId, String tokenHash, Instant expiresAt) {
        PasswordResetTokenEntity entity = new PasswordResetTokenEntity();
        entity.setUserId(userId);
        entity.setTokenHash(tokenHash);
        entity.setExpiresAt(expiresAt);
        entity.setUsed(false);
        PasswordResetTokenEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<StoredResetToken> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash).map(this::toDomain);
    }

    @Override
    public void markUsed(Long id) {
        jpaRepository.findById(id).ifPresent(entity -> {
            entity.setUsed(true);
            jpaRepository.save(entity);
        });
    }

    private StoredResetToken toDomain(PasswordResetTokenEntity entity) {
        return new StoredResetToken(entity.getId(), entity.getUserId(), entity.getTokenHash(), entity.getExpiresAt(), entity.isUsed());
    }
}
