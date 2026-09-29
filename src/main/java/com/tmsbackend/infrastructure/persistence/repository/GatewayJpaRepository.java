package com.tmsbackend.infrastructure.persistence.repository;

import com.tmsbackend.infrastructure.persistence.entity.GatewayEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatewayJpaRepository extends JpaRepository<GatewayEntity, String> {
}
