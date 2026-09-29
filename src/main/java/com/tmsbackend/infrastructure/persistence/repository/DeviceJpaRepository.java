package com.tmsbackend.infrastructure.persistence.repository;

import com.tmsbackend.infrastructure.persistence.entity.DeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceJpaRepository extends JpaRepository<DeviceEntity, String> {
}
