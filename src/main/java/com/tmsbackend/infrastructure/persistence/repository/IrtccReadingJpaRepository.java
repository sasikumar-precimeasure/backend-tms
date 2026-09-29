package com.tmsbackend.infrastructure.persistence.repository;

import com.tmsbackend.infrastructure.persistence.entity.IrtccReadingEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IrtccReadingJpaRepository extends JpaRepository<IrtccReadingEntity, Long> {
    Optional<IrtccReadingEntity> findFirstByDeviceIdOrderByRecordedAtDesc(String deviceId);

    Optional<IrtccReadingEntity> findByDeviceIdAndRecordedAt(String deviceId, java.time.Instant recordedAt);
}
