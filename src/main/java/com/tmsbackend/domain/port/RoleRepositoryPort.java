package com.tmsbackend.domain.port;

import com.tmsbackend.domain.model.Permission;
import com.tmsbackend.domain.model.Role;
import java.util.List;
import java.util.Optional;

public interface RoleRepositoryPort {
    Optional<Role> findById(Long id);

    List<Role> findAll();

    Role save(Role role);

    void replacePermissions(Long roleId, List<Permission> permissions);

    void deleteById(Long id);
}
