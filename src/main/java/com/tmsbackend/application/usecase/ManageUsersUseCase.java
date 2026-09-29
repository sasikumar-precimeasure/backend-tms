package com.tmsbackend.application.usecase;

import com.tmsbackend.application.audit.Auditable;
import com.tmsbackend.domain.model.AuditEventType;
import com.tmsbackend.domain.model.User;
import com.tmsbackend.domain.port.PasswordHasherPort;
import com.tmsbackend.domain.port.RefreshTokenRepositoryPort;
import com.tmsbackend.domain.port.UserRepositoryPort;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

// Backs the (backend-only, for now - no frontend UI yet) super-admin user
// management API. "Super admin" isn't a hardcoded role check here - callers
// reach this use case only after the web layer's @PreAuthorize gate on the
// caller's own Users-menu write permission already passed.
//
// Every mutating method here is @Auditable - AuditAspect writes the actual
// AuditEvent row after the method returns successfully (see that class and
// Auditable's javadoc for the actor-resolution and SpEL rules), so none of
// these methods touch AuditEventRepositoryPort directly any more.
@Component
public class ManageUsersUseCase {
    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException() {
            super("User not found");
        }
    }

    public static class DuplicateUserException extends RuntimeException {
        public DuplicateUserException(String message) {
            super(message);
        }
    }

    public static class CannotDeleteSelfException extends RuntimeException {
        public CannotDeleteSelfException() {
            super("You cannot delete your own account");
        }
    }

    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;
    private final RefreshTokenRepositoryPort refreshTokenRepository;

    public ManageUsersUseCase(
            UserRepositoryPort userRepository,
            PasswordHasherPort passwordHasher,
            RefreshTokenRepositoryPort refreshTokenRepository) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public List<User> listAll() {
        return userRepository.findAll();
    }

    @Auditable(
            type = AuditEventType.USER_CREATED,
            fieldName = "'userName'",
            newValue = "#userName",
            description = "'Created user ' + #userName")
    public User createUser(
            Long actingAdminId,
            String userName,
            String fullName,
            String email,
            String mobile,
            String rawPassword,
            Long roleId) {
        userRepository.findByUserName(userName).ifPresent(u -> {
            throw new DuplicateUserException("Username already in use");
        });
        userRepository.findByEmail(email).ifPresent(u -> {
            throw new DuplicateUserException("Email already in use");
        });

        return userRepository.save(new User(
                null, userName, fullName, email, mobile,
                passwordHasher.hash(rawPassword), true, Instant.now(), null,
                roleId, null, List.of(), null, null, null));
    }

    @Auditable(
            type = AuditEventType.USER_UPDATED,
            description = "'Updated user ' + #result.userName()")
    public User updateUser(Long actingAdminId, Long userId, String fullName, String email, String mobile, Long roleId) {
        User existing = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        User updated = new User(
                existing.id(), existing.userName(), fullName, email, mobile,
                existing.passwordHash(), existing.status(), existing.createdDate(), existing.lastLoginAt(),
                roleId, existing.role(), existing.permissions(), existing.imgUrl(), existing.timezone(),
                existing.dateFormat());
        return userRepository.save(updated);
    }

    @Auditable(
            type = AuditEventType.USER_STATUS_CHANGED,
            fieldName = "'status'",
            newValue = "#enabled",
            description = "(#enabled ? 'Enabled' : 'Disabled') + ' user ' + #result.userName()")
    public User setUserStatus(Long actingAdminId, Long userId, boolean enabled) {
        User existing = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        User updated = new User(
                existing.id(), existing.userName(), existing.fullName(), existing.email(), existing.mobile(),
                existing.passwordHash(), enabled, existing.createdDate(), existing.lastLoginAt(),
                existing.roleId(), existing.role(), existing.permissions(), existing.imgUrl(), existing.timezone(),
                existing.dateFormat());
        User saved = userRepository.save(updated);

        if (!enabled) {
            // Disabling a user must take effect immediately, not just on
            // their access token's natural expiry.
            refreshTokenRepository.revokeAllForUser(userId);
        }
        return saved;
    }

    @Auditable(
            type = AuditEventType.USER_DELETED,
            oldValue = "#result.userName()",
            description = "'Deleted user ' + #result.userName()")
    public User deleteUser(Long actingAdminId, Long userId) {
        if (userId.equals(actingAdminId)) {
            throw new CannotDeleteSelfException();
        }
        User existing = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        // Revoke sessions first - once the row is gone there's nothing left
        // to look up a user id against, but any refresh token issued to
        // them should stop working immediately either way.
        refreshTokenRepository.revokeAllForUser(userId);
        userRepository.deleteById(userId);
        return existing;
    }
}
