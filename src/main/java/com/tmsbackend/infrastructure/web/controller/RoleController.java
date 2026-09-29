package com.tmsbackend.infrastructure.web.controller;

import com.tmsbackend.application.usecase.ManageRolesUseCase;
import com.tmsbackend.infrastructure.web.CurrentUserResolver;
import com.tmsbackend.infrastructure.web.PermissionGuard;
import com.tmsbackend.infrastructure.web.dto.CreateRoleRequestDto;
import com.tmsbackend.infrastructure.web.dto.RoleDto;
import com.tmsbackend.infrastructure.web.dto.SetPermissionsRequestDto;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tms/api/roles")
public class RoleController {
    private static final String MENU = "Roles";

    private final ManageRolesUseCase manageRolesUseCase;
    private final PermissionGuard permissionGuard;
    private final CurrentUserResolver currentUserResolver;

    public RoleController(ManageRolesUseCase manageRolesUseCase, PermissionGuard permissionGuard, CurrentUserResolver currentUserResolver) {
        this.manageRolesUseCase = manageRolesUseCase;
        this.permissionGuard = permissionGuard;
        this.currentUserResolver = currentUserResolver;
    }

    // Read-gated (not write) - the Users screen's role dropdown and this
    // screen's own list both just need visibility, same reasoning as Audit
    // Log's GET endpoints.
    @GetMapping
    public List<RoleDto> list() {
        permissionGuard.requireRead(currentUserResolver.requireUserId(), MENU);
        return manageRolesUseCase.listAll().stream().map(RoleDto::from).toList();
    }

    @PostMapping
    public RoleDto create(@Valid @RequestBody CreateRoleRequestDto request) {
        Long callerId = currentUserResolver.requireUserId();
        permissionGuard.requireWrite(callerId, MENU);
        return RoleDto.from(manageRolesUseCase.createRole(callerId, request.name()));
    }

    @PutMapping("/{id}/permissions")
    public void setPermissions(@PathVariable Long id, @RequestBody SetPermissionsRequestDto request) {
        permissionGuard.requireWrite(currentUserResolver.requireUserId(), MENU);
        manageRolesUseCase.setPermissions(id, request.permissions());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        Long callerId = currentUserResolver.requireUserId();
        permissionGuard.requireWrite(callerId, MENU);
        manageRolesUseCase.deleteRole(callerId, id);
    }
}
