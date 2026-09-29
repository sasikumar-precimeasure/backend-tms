package com.tmsbackend.infrastructure.web;

import com.tmsbackend.domain.model.User;
import com.tmsbackend.domain.port.UserRepositoryPort;
import org.springframework.stereotype.Component;

// "Super admin" isn't a hardcoded role check anywhere in this codebase - a
// caller may perform a menu's write action only if their role's own
// Permission list grants write=true for that menu. The seed migration gives
// the "Super Admin" role write on every menu (including "Users"), so the
// admin API is reachable only by whoever holds that role or an
// equivalently-provisioned one - consistent with the existing frontend
// permission model in User.ts.
@Component
public class PermissionGuard {
    public static class ForbiddenException extends RuntimeException {
        public ForbiddenException(String message) {
            super(message);
        }
    }

    private final UserRepositoryPort userRepository;

    public PermissionGuard(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    public void requireWrite(Long userId, String menu) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ForbiddenException("Not authorized"));
        boolean hasWrite = user.permissions().stream()
                .anyMatch(p -> p.menu().equalsIgnoreCase(menu) && p.write());
        if (!hasWrite) {
            throw new ForbiddenException("You do not have permission to manage " + menu);
        }
    }

    // Read-only gate (e.g. viewing the Audit Log) - a caller with either
    // read or write on the menu passes, since write implies read in this
    // app's model (there's no case where someone can edit a menu's data but
    // not see it).
    public void requireRead(Long userId, String menu) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ForbiddenException("Not authorized"));
        boolean hasRead = user.permissions().stream()
                .anyMatch(p -> p.menu().equalsIgnoreCase(menu) && (p.read() || p.write()));
        if (!hasRead) {
            throw new ForbiddenException("You do not have permission to view " + menu);
        }
    }
}
