package com.tmsbackend.infrastructure.web.controller;

import com.tmsbackend.application.dto.AuthResult;
import com.tmsbackend.application.usecase.ForgotPasswordUseCase;
import com.tmsbackend.application.usecase.GetCurrentUserUseCase;
import com.tmsbackend.application.usecase.LoginUseCase;
import com.tmsbackend.application.usecase.LogoutUseCase;
import com.tmsbackend.application.usecase.RefreshTokenUseCase;
import com.tmsbackend.application.usecase.ResetPasswordUseCase;
import com.tmsbackend.infrastructure.web.CurrentUserResolver;
import com.tmsbackend.infrastructure.web.dto.ApiResponseDto;
import com.tmsbackend.infrastructure.web.dto.AuthResponseDto;
import com.tmsbackend.infrastructure.web.dto.ForgotPasswordRequestDto;
import com.tmsbackend.infrastructure.web.dto.LoginRequestDto;
import com.tmsbackend.infrastructure.web.dto.RefreshTokenRequestDto;
import com.tmsbackend.infrastructure.web.dto.ResetPasswordRequestDto;
import com.tmsbackend.infrastructure.web.dto.UserDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Every route here matches tms/src/infrastructure/repositories/AuthRepositoryImpl.ts
// and client.ts exactly - base path, response shapes (login/refresh
// unwrapped, everything else in ApiResponseDto), field names.
@RestController
@RequestMapping("/tms/api")
public class AuthController {
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final CurrentUserResolver currentUserResolver;

    public AuthController(
            LoginUseCase loginUseCase,
            RefreshTokenUseCase refreshTokenUseCase,
            LogoutUseCase logoutUseCase,
            ForgotPasswordUseCase forgotPasswordUseCase,
            ResetPasswordUseCase resetPasswordUseCase,
            GetCurrentUserUseCase getCurrentUserUseCase,
            CurrentUserResolver currentUserResolver) {
        this.loginUseCase = loginUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUseCase = logoutUseCase;
        this.forgotPasswordUseCase = forgotPasswordUseCase;
        this.resetPasswordUseCase = resetPasswordUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.currentUserResolver = currentUserResolver;
    }

    @PostMapping("/auth/login")
    public AuthResponseDto login(@Valid @RequestBody LoginRequestDto request) {
        AuthResult result = loginUseCase.execute(request.username(), request.password());
        return AuthResponseDto.from(result);
    }

    @PostMapping("/auth/refresh")
    public AuthResponseDto refresh(@Valid @RequestBody RefreshTokenRequestDto request) {
        AuthResult result = refreshTokenUseCase.execute(request.refreshToken());
        return AuthResponseDto.from(result);
    }

    @PostMapping("/auth/logout")
    public void logout(@RequestBody(required = false) RefreshTokenRequestDto request) {
        if (request != null) {
            logoutUseCase.execute(request.refreshToken());
        }
    }

    @PostMapping("/auth/forgot-password")
    public ApiResponseDto<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto request) {
        forgotPasswordUseCase.execute(request.email());
        return ApiResponseDto.ok(null, "If that email is registered, a reset link has been sent.");
    }

    @PostMapping("/auth/reset-password")
    public ApiResponseDto<Void> resetPassword(@Valid @RequestBody ResetPasswordRequestDto request) {
        resetPasswordUseCase.execute(request.token(), request.newPassword(), request.confirmPassword());
        return ApiResponseDto.ok(null, "Password has been reset successfully.");
    }

    @GetMapping("/users/me")
    public ApiResponseDto<UserDto> me() {
        var user = getCurrentUserUseCase.execute(currentUserResolver.requireUserId());
        return ApiResponseDto.ok(UserDto.from(user), "OK");
    }
}
