package com.tmsbackend.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequestDto(@NotBlank String newPassword, @NotBlank String confirmPassword, @NotBlank String token) {
}
