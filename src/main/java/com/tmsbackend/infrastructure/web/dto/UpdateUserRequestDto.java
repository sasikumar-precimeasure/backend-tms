package com.tmsbackend.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequestDto(String fullName, @NotBlank @Email String email, String mobile, Long roleId) {
}
