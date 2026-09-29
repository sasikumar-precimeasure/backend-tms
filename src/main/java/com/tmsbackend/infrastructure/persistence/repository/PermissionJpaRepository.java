package com.tmsbackend.infrastructure.persistence.repository;

import com.tmsbackend.infrastructure.persistence.entity.PermissionEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PermissionJpaRepository extends JpaRepository<PermissionEntity, Long> {
    List<PermissionEntity> findByRoleId(Long roleId);

    @Modifying
    @Query("delete from PermissionEntity p where p.role.id = :roleId")
    void deleteAllByRoleId(@Param("roleId") Long roleId);
}
