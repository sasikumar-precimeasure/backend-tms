package com.tmsbackend.infrastructure.web.dto;

import java.time.Instant;

// Matches tms/src/domain/entities/User.ts's ApiResponse<T> envelope exactly.
public record ApiResponseDto<T>(boolean success, String title, String message, T data, String timestamp) {
    public static <T> ApiResponseDto<T> ok(T data, String message) {
        return new ApiResponseDto<>(true, null, message, data, Instant.now().toString());
    }

    public static <T> ApiResponseDto<T> error(String message) {
        return new ApiResponseDto<>(false, null, message, null, Instant.now().toString());
    }
}
