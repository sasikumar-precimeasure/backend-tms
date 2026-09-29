package com.tmsbackend.infrastructure.web.controller;

import com.tmsbackend.application.usecase.ManageUsersUseCase;
import com.tmsbackend.infrastructure.web.CurrentUserResolver;
import com.tmsbackend.infrastructure.web.PermissionGuard;
import com.tmsbackend.infrastructure.web.dto.CreateUserRequestDto;
import com.tmsbackend.infrastructure.web.dto.SetUserStatusRequestDto;
import com.tmsbackend.infrastructure.web.dto.UpdateUserRequestDto;
import com.tmsbackend.infrastructure.web.dto.UserDto;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Backend-only for now (no frontend UI exists yet) - the super-admin user
// management API. Every mutating action is gated on the caller's own
// write permission for the "Users" menu (see PermissionGuard), not a
// hardcoded role name.
@RestController
@RequestMapping("/tms/api/users")
public class UserController {
    private static final String MENU = "Users";

    private final ManageUsersUseCase manageUsersUseCase;
    private final PermissionGuard permissionGuard;
    private final CurrentUserResolver currentUserResolver;

    public UserController(ManageUsersUseCase manageUsersUseCase, PermissionGuard permissionGuard, CurrentUserResolver currentUserResolver) {
        this.manageUsersUseCase = manageUsersUseCase;
        this.permissionGuard = permissionGuard;
        this.currentUserResolver = currentUserResolver;
    }

    @GetMapping
    public List<UserDto> list() {
        Long callerId = currentUserResolver.requireUserId();
        permissionGuard.requireWrite(callerId, MENU);
        return manageUsersUseCase.listAll().stream().map(UserDto::from).toList();
    }

    @PostMapping
    public UserDto create(@Valid @RequestBody CreateUserRequestDto request) {
        Long callerId = currentUserResolver.requireUserId();
        permissionGuard.requireWrite(callerId, MENU);
        var created = manageUsersUseCase.createUser(
                callerId, request.userName(), request.fullName(), request.email(), request.mobile(),
                request.password(), request.roleId());
        return UserDto.from(created);
    }

    @PatchMapping("/{id}")
    public UserDto update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequestDto request) {
        Long callerId = currentUserResolver.requireUserId();
        permissionGuard.requireWrite(callerId, MENU);
        var updated = manageUsersUseCase.updateUser(callerId, id, request.fullName(), request.email(), request.mobile(), request.roleId());
        return UserDto.from(updated);
    }

    @PatchMapping("/{id}/status")
    public UserDto setStatus(@PathVariable Long id, @RequestBody SetUserStatusRequestDto request) {
        Long callerId = currentUserResolver.requireUserId();
        permissionGuard.requireWrite(callerId, MENU);
        var updated = manageUsersUseCase.setUserStatus(callerId, id, request.enabled());
        return UserDto.from(updated);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        Long callerId = currentUserResolver.requireUserId();
        permissionGuard.requireWrite(callerId, MENU);
        manageUsersUseCase.deleteUser(callerId, id);
    }
}
