package com.tmsbackend.infrastructure.persistence.repository;

import com.tmsbackend.infrastructure.persistence.entity.Device2243ReadingEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Device2243ReadingJpaRepository extends JpaRepository<Device2243ReadingEntity, Long> {
    Optional<Device2243ReadingEntity> findFirstByDeviceIdOrderByRecordedAtDesc(String deviceId);

    Optional<Device2243ReadingEntity> findByDeviceIdAndRecordedAt(String deviceId, java.time.Instant recordedAt);
}
