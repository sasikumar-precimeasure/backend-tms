package com.tmsbackend.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequestDto(
        @NotBlank String userName, String fullName, @NotBlank @Email String email, String mobile,
        @NotBlank String password, Long roleId) {
}
