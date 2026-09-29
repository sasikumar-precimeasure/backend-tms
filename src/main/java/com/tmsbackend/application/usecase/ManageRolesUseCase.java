package com.tmsbackend.application.usecase;

import com.tmsbackend.application.audit.Auditable;
import com.tmsbackend.domain.model.AuditEventType;
import com.tmsbackend.domain.model.Permission;
import com.tmsbackend.domain.model.Role;
import com.tmsbackend.domain.port.RoleRepositoryPort;
import com.tmsbackend.domain.port.UserRepositoryPort;
import java.util.List;
import org.springframework.stereotype.Component;

// @Auditable methods below have their AuditEvent row written by AuditAspect
// after the method returns - see that class and Auditable's javadoc.
@Component
public class ManageRolesUseCase {
    public static class RoleNotFoundException extends RuntimeException {
        public RoleNotFoundException() {
            super("Role not found");
        }
    }

    public static class RoleInUseException extends RuntimeException {
        public RoleInUseException(long userCount) {
            super("Cannot delete this role - it is still assigned to " + userCount
                    + " user(s). Reassign them to a different role first.");
        }
    }

    private final RoleRepositoryPort roleRepository;
    private final UserRepositoryPort userRepository;

    public ManageRolesUseCase(RoleRepositoryPort roleRepository, UserRepositoryPort userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    public List<Role> listAll() {
        return roleRepository.findAll();
    }

    @Auditable(
            type = AuditEventType.ROLE_CREATED,
            newValue = "#name",
            description = "'Created role ' + #name")
    public Role createRole(Long actingUserId, String name) {
        return roleRepository.save(new Role(null, name, true, List.of()));
    }

    public void setPermissions(Long roleId, List<Permission> permissions) {
        roleRepository.replacePermissions(roleId, permissions);
    }

    @Auditable(
            type = AuditEventType.ROLE_DELETED,
            oldValue = "#result.name()",
            description = "'Deleted role ' + #result.name()")
    public Role deleteRole(Long actingUserId, Long roleId) {
        Role existing = roleRepository.findById(roleId).orElseThrow(RoleNotFoundException::new);
        long usersWithRole = userRepository.countByRoleId(roleId);
        if (usersWithRole > 0) {
            throw new RoleInUseException(usersWithRole);
        }
        roleRepository.deleteById(roleId);
        return existing;
    }
}
