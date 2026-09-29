package com.tmsbackend.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

// Matches tms/src/domain/entities/User.ts's LoginCredentials exactly.
public record LoginRequestDto(@NotBlank String username, @NotBlank String password, boolean rememberMe) {
}
