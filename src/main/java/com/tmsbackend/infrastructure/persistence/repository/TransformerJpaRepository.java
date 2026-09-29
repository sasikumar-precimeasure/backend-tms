package com.tmsbackend.infrastructure.persistence.repository;

import com.tmsbackend.infrastructure.persistence.entity.TransformerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransformerJpaRepository extends JpaRepository<TransformerEntity, String> {
}
