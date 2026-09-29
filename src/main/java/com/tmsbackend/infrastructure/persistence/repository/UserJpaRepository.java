package com.tmsbackend.infrastructure.persistence.repository;

import com.tmsbackend.infrastructure.persistence.entity.UserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUserName(String userName);

    Optional<UserEntity> findByEmail(String email);

    long countByRole_Id(Long roleId);
}
