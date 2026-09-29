package com.tmsbackend.infrastructure.persistence.repository;

import com.tmsbackend.infrastructure.persistence.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleJpaRepository extends JpaRepository<RoleEntity, Long> {
}
