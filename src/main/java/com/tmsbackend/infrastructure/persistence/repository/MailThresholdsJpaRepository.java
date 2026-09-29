package com.tmsbackend.infrastructure.persistence.repository;

import com.tmsbackend.infrastructure.persistence.entity.MailThresholdsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MailThresholdsJpaRepository extends JpaRepository<MailThresholdsEntity, String> {
}
