package com.tmsbackend.infrastructure.persistence.repository;

import com.tmsbackend.infrastructure.persistence.entity.AuditEventEntity;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventJpaRepository extends JpaRepository<AuditEventEntity, Long> {
    List<AuditEventEntity> findAllByOrderByOccurredAtDesc(Pageable pageable);

    List<AuditEventEntity> findByDeviceIdOrderByOccurredAtDesc(String deviceId, Pageable pageable);

    List<AuditEventEntity> findByUserIdOrderByOccurredAtDesc(Long userId, Pageable pageable);

    List<AuditEventEntity> findByOccurredAtBetweenOrderByOccurredAtDesc(Instant from, Instant to, Pageable pageable);

    List<AuditEventEntity> findByDeviceIdAndOccurredAtBetweenOrderByOccurredAtDesc(
            String deviceId, Instant from, Instant to, Pageable pageable);
}
