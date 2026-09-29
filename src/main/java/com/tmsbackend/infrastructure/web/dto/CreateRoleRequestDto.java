package com.tmsbackend.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateRoleRequestDto(@NotBlank String name) {
}
