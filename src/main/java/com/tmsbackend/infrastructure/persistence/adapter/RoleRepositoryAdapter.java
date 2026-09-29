package com.tmsbackend.infrastructure.persistence.adapter;

import com.tmsbackend.domain.model.Permission;
import com.tmsbackend.domain.model.Role;
import com.tmsbackend.domain.port.RoleRepositoryPort;
import com.tmsbackend.infrastructure.persistence.entity.PermissionEntity;
import com.tmsbackend.infrastructure.persistence.entity.RoleEntity;
import com.tmsbackend.infrastructure.persistence.repository.PermissionJpaRepository;
import com.tmsbackend.infrastructure.persistence.repository.RoleJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RoleRepositoryAdapter implements RoleRepositoryPort {
    private final RoleJpaRepository roleJpaRepository;
    private final PermissionJpaRepository permissionJpaRepository;

    public RoleRepositoryAdapter(RoleJpaRepository roleJpaRepository, PermissionJpaRepository permissionJpaRepository) {
        this.roleJpaRepository = roleJpaRepository;
        this.permissionJpaRepository = permissionJpaRepository;
    }

    @Override
    public Optional<Role> findById(Long id) {
        return roleJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Role> findAll() {
        return roleJpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public Role save(Role role) {
        RoleEntity entity = role.id() != null ? roleJpaRepository.findById(role.id()).orElseGet(RoleEntity::new) : new RoleEntity();
        entity.setName(role.name());
        entity.setStatus(role.status());
        RoleEntity saved = roleJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional
    public void replacePermissions(Long roleId, List<Permission> permissions) {
        permissionJpaRepository.deleteAllByRoleId(roleId);
        RoleEntity role = roleJpaRepository.findById(roleId).orElseThrow();
        for (Permission permission : permissions) {
            PermissionEntity entity = new PermissionEntity();
            entity.setRole(role);
            entity.setMenu(permission.menu());
            entity.setFunction(permission.function());
            entity.setCanRead(permission.read());
            entity.setCanWrite(permission.write());
            permissionJpaRepository.save(entity);
        }
    }

    @Override
    public void deleteById(Long id) {
        roleJpaRepository.deleteById(id);
    }

    private Role toDomain(RoleEntity entity) {
        List<Permission> permissions = entity.getPermissions().stream()
                .map(p -> new Permission(p.getMenu(), p.getFunction(), p.isCanRead(), p.isCanWrite()))
                .toList();
        return new Role(entity.getId(), entity.getName(), entity.isStatus(), permissions);
    }
}
