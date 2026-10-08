package com.tmsbackend.infrastructure.persistence.repository;

import com.tmsbackend.infrastructure.persistence.entity.ReportSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportSettingsJpaRepository extends JpaRepository<ReportSettingsEntity, Integer> {
}
