package com.tmsbackend.infrastructure.persistence.repository;

import com.tmsbackend.infrastructure.persistence.entity.IrtccReadingEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IrtccReadingJpaRepository extends JpaRepository<IrtccReadingEntity, Long> {
    Optional<IrtccReadingEntity> findFirstByDeviceIdOrderByRecordedAtDesc(String deviceId);

    Optional<IrtccReadingEntity> findByDeviceIdAndRecordedAt(String deviceId, Instant recordedAt);

    Page<IrtccReadingEntity> findByDeviceIdAndRecordedAtBetweenOrderByRecordedAtDesc(
            String deviceId, Instant from, Instant to, Pageable pageable);

    List<IrtccReadingEntity> findByDeviceIdAndRecordedAtBetweenOrderByRecordedAtDesc(String deviceId, Instant from, Instant to);
}
