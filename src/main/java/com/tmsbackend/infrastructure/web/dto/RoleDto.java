package com.tmsbackend.infrastructure.web.dto;

import com.tmsbackend.domain.model.Permission;
import com.tmsbackend.domain.model.Role;
import java.util.List;

public record RoleDto(Long id, String name, boolean status, List<Permission> permissions) {
    public static RoleDto from(Role role) {
        return new RoleDto(role.id(), role.name(), role.status(), role.permissions());
    }
}
