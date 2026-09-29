package com.tmsbackend.application.dto;

import com.tmsbackend.domain.model.User;

// Framework-agnostic result shape for login/refresh - the web layer maps
// this onto the exact frontend-facing LoginResponse/RefreshTokenResponse
// JSON shape.
public record AuthResult(String accessToken, String refreshToken, long expiresInSeconds, User user) {
}
