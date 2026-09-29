package com.tmsbackend.infrastructure.web.dto;

import com.tmsbackend.application.dto.AuthResult;

// Matches tms/src/domain/entities/User.ts's LoginResponse/RefreshTokenResponse
// exactly - returned UNWRAPPED (not inside ApiResponse), confirmed by the
// frontend's loginAsync reading response.accessToken directly.
public record AuthResponseDto(String accessToken, String refreshToken, String tokenType, long expiresIn, UserDto user) {
    public static AuthResponseDto from(AuthResult result) {
        return new AuthResponseDto(result.accessToken(), result.refreshToken(), "Bearer", result.expiresInSeconds(), UserDto.from(result.user()));
    }
}
