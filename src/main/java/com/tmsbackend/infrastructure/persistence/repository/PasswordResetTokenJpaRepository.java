package com.tmsbackend.infrastructure.persistence.repository;

import com.tmsbackend.infrastructure.persistence.entity.PasswordResetTokenEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenJpaRepository extends JpaRepository<PasswordResetTokenEntity, Long> {
    Optional<PasswordResetTokenEntity> findByTokenHash(String tokenHash);
}
