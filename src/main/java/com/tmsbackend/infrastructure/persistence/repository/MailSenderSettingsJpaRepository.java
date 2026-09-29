package com.tmsbackend.infrastructure.persistence.repository;

import com.tmsbackend.infrastructure.persistence.entity.MailSenderSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MailSenderSettingsJpaRepository extends JpaRepository<MailSenderSettingsEntity, Integer> {
}
