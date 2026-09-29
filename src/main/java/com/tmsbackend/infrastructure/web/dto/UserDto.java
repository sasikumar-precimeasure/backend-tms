package com.tmsbackend.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tmsbackend.domain.model.Permission;
import com.tmsbackend.domain.model.Role;
import com.tmsbackend.domain.model.User;
import java.time.Instant;
import java.util.List;

// Matches tms/src/domain/entities/User.ts's User interface field-for-field,
// including the one snake_case field (created_date) the existing frontend
// contract already uses.
public record UserDto(
        Long id,
        String userName,
        String fullName,
        String email,
        String mobile,
        boolean status,
        @JsonProperty("created_date") Instant createdDate,
        Instant lastLoginAt,
        Long roleId,
        RoleDto role,
        List<Permission> permissions,
        String imgUrl,
        String timezone,
        String dateFormat) {

    public record RoleDto(Long id, String name, boolean status) {
    }

    public static UserDto from(User user) {
        RoleDto roleDto = user.role() != null ? new RoleDto(user.role().id(), user.role().name(), user.role().status()) : null;
        return new UserDto(
                user.id(), user.userName(), user.fullName(), user.email(), user.mobile(), user.status(),
                user.createdDate(), user.lastLoginAt(), user.roleId(), roleDto, user.permissions(),
                user.imgUrl(), user.timezone(), user.dateFormat());
    }
}
