package com.tmsbackend.infrastructure.persistence.repository;

import com.tmsbackend.infrastructure.persistence.entity.ReportRecipientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRecipientJpaRepository extends JpaRepository<ReportRecipientEntity, String> {
}
