package com.tmsbackend.infrastructure.persistence.repository;

import com.tmsbackend.infrastructure.persistence.entity.MailLastSentEntity;
import com.tmsbackend.infrastructure.persistence.entity.MailLastSentId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MailLastSentJpaRepository extends JpaRepository<MailLastSentEntity, MailLastSentId> {
}
