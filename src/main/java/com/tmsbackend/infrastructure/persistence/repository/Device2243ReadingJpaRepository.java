package com.tmsbackend.infrastructure.persistence.repository;

import com.tmsbackend.infrastructure.persistence.entity.Device2243ReadingEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Device2243ReadingJpaRepository extends JpaRepository<Device2243ReadingEntity, Long> {
    Optional<Device2243ReadingEntity> findFirstByDeviceIdOrderByRecordedAtDesc(String deviceId);

    Optional<Device2243ReadingEntity> findByDeviceIdAndRecordedAt(String deviceId, Instant recordedAt);

    Page<Device2243ReadingEntity> findByDeviceIdAndRecordedAtBetweenOrderByRecordedAtDesc(
            String deviceId, Instant from, Instant to, Pageable pageable);

    List<Device2243ReadingEntity> findByDeviceIdAndRecordedAtBetweenOrderByRecordedAtDesc(String deviceId, Instant from, Instant to);
}
