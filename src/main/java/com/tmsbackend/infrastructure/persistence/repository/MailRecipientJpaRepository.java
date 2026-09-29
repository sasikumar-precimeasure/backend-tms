package com.tmsbackend.infrastructure.persistence.repository;

import com.tmsbackend.infrastructure.persistence.entity.MailRecipientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MailRecipientJpaRepository extends JpaRepository<MailRecipientEntity, String> {
}
