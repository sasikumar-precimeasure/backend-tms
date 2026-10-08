package com.tmsbackend.infrastructure.web.advice;

import com.tmsbackend.application.usecase.ForgotPasswordUseCase;
import com.tmsbackend.application.usecase.GetCurrentUserUseCase;
import com.tmsbackend.application.usecase.GetDataLogUseCase;
import com.tmsbackend.application.usecase.LoginUseCase;
import com.tmsbackend.application.usecase.ManageMailSettingsUseCase;
import com.tmsbackend.application.usecase.ManageRolesUseCase;
import com.tmsbackend.application.usecase.ManageUsersUseCase;
import com.tmsbackend.application.usecase.MonthlyReportUseCase;
import com.tmsbackend.application.usecase.RefreshTokenUseCase;
import com.tmsbackend.application.usecase.ResetPasswordUseCase;
import com.tmsbackend.infrastructure.web.PermissionGuard;
import com.tmsbackend.infrastructure.web.dto.ApiResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Every error response uses the same ApiResponseDto envelope the frontend
// already expects for wrapped endpoints; unwrapped endpoints (login/refresh)
// still surface a JSON body with a "message" field on failure via Spring's
// default error handling, which the frontend's extractErrorMessage() reads
// off response.data.message.
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(LoginUseCase.InvalidCredentialsException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleInvalidCredentials(LoginUseCase.InvalidCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponseDto.error(e.getMessage()));
    }

    @ExceptionHandler(LoginUseCase.AccountDisabledException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleAccountDisabled(LoginUseCase.AccountDisabledException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponseDto.error(e.getMessage()));
    }

    @ExceptionHandler(RefreshTokenUseCase.InvalidRefreshTokenException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleInvalidRefreshToken(RefreshTokenUseCase.InvalidRefreshTokenException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponseDto.error(e.getMessage()));
    }

    @ExceptionHandler(ForgotPasswordUseCase.SeedAdminForgotPasswordException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleSeedAdminForgotPassword(ForgotPasswordUseCase.SeedAdminForgotPasswordException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponseDto.error(e.getMessage()));
    }

    @ExceptionHandler(ResetPasswordUseCase.InvalidResetTokenException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleInvalidResetToken(ResetPasswordUseCase.InvalidResetTokenException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponseDto.error(e.getMessage()));
    }

    @ExceptionHandler(ResetPasswordUseCase.PasswordMismatchException.class)
    public ResponseEntity<ApiResponseDto<Void>> handlePasswordMismatch(ResetPasswordUseCase.PasswordMismatchException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponseDto.error(e.getMessage()));
    }

    @ExceptionHandler(GetCurrentUserUseCase.UserNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleUserNotFound(GetCurrentUserUseCase.UserNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDto.error(e.getMessage()));
    }

    @ExceptionHandler(ManageUsersUseCase.UserNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleManageUserNotFound(ManageUsersUseCase.UserNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDto.error(e.getMessage()));
    }

    @ExceptionHandler(ManageUsersUseCase.DuplicateUserException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleDuplicateUser(ManageUsersUseCase.DuplicateUserException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponseDto.error(e.getMessage()));
    }

    @ExceptionHandler(ManageMailSettingsUseCase.DeviceNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleMailDeviceNotFound(ManageMailSettingsUseCase.DeviceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponseDto.error(e.getMessage()));
    }

    @ExceptionHandler(ManageUsersUseCase.CannotDeleteSelfException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleCannotDeleteSelf(ManageUsersUseCase.CannotDeleteSelfException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponseDto.error(e.getMessage()));
    }

    @ExceptionHandler(ManageRolesUseCase.RoleNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleRoleNotFound(ManageRolesUseCase.RoleNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDto.error(e.getMessage()));
    }

    @ExceptionHandler(ManageRolesUseCase.RoleInUseException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleRoleInUse(ManageRolesUseCase.RoleInUseException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponseDto.error(e.getMessage()));
    }

    @ExceptionHandler(GetDataLogUseCase.DeviceNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleDataLogDeviceNotFound(GetDataLogUseCase.DeviceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDto.error(e.getMessage()));
    }

    @ExceptionHandler(MonthlyReportUseCase.InvalidReportSettingsException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleInvalidReportSettings(MonthlyReportUseCase.InvalidReportSettingsException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponseDto.error(e.getMessage()));
    }

    @ExceptionHandler(MonthlyReportUseCase.ReportNotSendableException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleReportNotSendable(MonthlyReportUseCase.ReportNotSendableException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponseDto.error(e.getMessage()));
    }

    @ExceptionHandler(PermissionGuard.ForbiddenException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleForbidden(PermissionGuard.ForbiddenException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponseDto.error(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .orElse("Invalid request");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponseDto.error(message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleUnexpected(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponseDto.error("An unexpected error occurred"));
    }
}
