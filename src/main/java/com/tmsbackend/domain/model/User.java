package com.tmsbackend.domain.model;

import java.time.Instant;
import java.util.List;

// Mirrors tms/src/domain/entities/User.ts's User interface field-for-field
// (passwordHash is the one addition, never serialized to the frontend).
public record User(
        Long id,
        String userName,
        String fullName,
        String email,
        String mobile,
        String passwordHash,
        boolean status,
        Instant createdDate,
        Instant lastLoginAt,
        Long roleId,
        Role role,
        List<Permission> permissions,
        String imgUrl,
        String timezone,
        String dateFormat) {
}
