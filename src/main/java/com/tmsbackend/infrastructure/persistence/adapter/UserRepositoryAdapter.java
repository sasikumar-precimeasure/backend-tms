package com.tmsbackend.infrastructure.persistence.adapter;

import com.tmsbackend.domain.model.Permission;
import com.tmsbackend.domain.model.Role;
import com.tmsbackend.domain.model.User;
import com.tmsbackend.domain.port.UserRepositoryPort;
import com.tmsbackend.infrastructure.persistence.entity.PermissionEntity;
import com.tmsbackend.infrastructure.persistence.entity.RoleEntity;
import com.tmsbackend.infrastructure.persistence.entity.UserEntity;
import com.tmsbackend.infrastructure.persistence.repository.RoleJpaRepository;
import com.tmsbackend.infrastructure.persistence.repository.UserJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {
    private final UserJpaRepository userJpaRepository;
    private final RoleJpaRepository roleJpaRepository;

    public UserRepositoryAdapter(UserJpaRepository userJpaRepository, RoleJpaRepository roleJpaRepository) {
        this.userJpaRepository = userJpaRepository;
        this.roleJpaRepository = roleJpaRepository;
    }

    @Override
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByUserName(String userName) {
        return userJpaRepository.findByUserName(userName).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public List<User> findAll() {
        return userJpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public User save(User user) {
        UserEntity entity = user.id() != null
                ? userJpaRepository.findById(user.id()).orElseGet(UserEntity::new)
                : new UserEntity();

        entity.setUserName(user.userName());
        entity.setFullName(user.fullName());
        entity.setEmail(user.email());
        entity.setMobile(user.mobile());
        entity.setPasswordHash(user.passwordHash());
        entity.setStatus(user.status());
        entity.setCreatedDate(entity.getCreatedDate() != null ? entity.getCreatedDate() : user.createdDate());
        entity.setLastLoginAt(user.lastLoginAt());
        entity.setImgUrl(user.imgUrl());
        entity.setTimezone(user.timezone());
        entity.setDateFormat(user.dateFormat());

        if (user.roleId() != null) {
            RoleEntity role = roleJpaRepository.findById(user.roleId()).orElse(null);
            entity.setRole(role);
        } else {
            entity.setRole(null);
        }

        if (entity.getCreatedDate() == null) {
            entity.setCreatedDate(Instant.now());
        }

        UserEntity saved = userJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void updateLastLoginAt(Long userId, Instant when) {
        userJpaRepository.findById(userId).ifPresent(entity -> {
            entity.setLastLoginAt(when);
            userJpaRepository.save(entity);
        });
    }

    @Override
    public void deleteById(Long id) {
        userJpaRepository.deleteById(id);
    }

    @Override
    public long countByRoleId(Long roleId) {
        return userJpaRepository.countByRole_Id(roleId);
    }

    private User toDomain(UserEntity entity) {
        Role role = entity.getRole() != null ? toDomainRole(entity.getRole()) : null;
        List<Permission> permissions = role != null ? role.permissions() : List.of();
        return new User(
                entity.getId(),
                entity.getUserName(),
                entity.getFullName(),
                entity.getEmail(),
                entity.getMobile(),
                entity.getPasswordHash(),
                entity.isStatus(),
                entity.getCreatedDate(),
                entity.getLastLoginAt(),
                entity.getRole() != null ? entity.getRole().getId() : null,
                role,
                permissions,
                entity.getImgUrl(),
                entity.getTimezone(),
                entity.getDateFormat());
    }

    private Role toDomainRole(RoleEntity entity) {
        List<Permission> permissions = entity.getPermissions().stream()
                .map(this::toDomainPermission)
                .toList();
        return new Role(entity.getId(), entity.getName(), entity.isStatus(), permissions);
    }

    private Permission toDomainPermission(PermissionEntity entity) {
        return new Permission(entity.getMenu(), entity.getFunction(), entity.isCanRead(), entity.isCanWrite());
    }
}
