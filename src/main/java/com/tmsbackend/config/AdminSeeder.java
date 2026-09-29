package com.tmsbackend.config;

import com.tmsbackend.domain.model.Role;
import com.tmsbackend.domain.model.User;
import com.tmsbackend.domain.port.PasswordHasherPort;
import com.tmsbackend.domain.port.RoleRepositoryPort;
import com.tmsbackend.domain.port.UserRepositoryPort;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

// Seeds the initial super-admin login on first boot (only if no users exist
// yet, so this is a no-op on every subsequent restart) - the "Super Admin"
// role itself is created by the V1 Flyway migration with full permissions
// across every menu; this only creates the human account that holds it.
// Password comes from ADMIN_SEED_PASSWORD (default "admin123" for local
// dev) - each client install should override this env var before first
// boot, or change the password immediately after first login.
@Component
public class AdminSeeder implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);
    private static final String SUPER_ADMIN_ROLE_NAME = "Super Admin";

    private final UserRepositoryPort userRepository;
    private final RoleRepositoryPort roleRepository;
    private final PasswordHasherPort passwordHasher;
    private final String seedUsername;
    private final String seedEmail;
    private final String seedPassword;

    public AdminSeeder(
            UserRepositoryPort userRepository,
            RoleRepositoryPort roleRepository,
            PasswordHasherPort passwordHasher,
            @Value("${tms.admin-seed.username}") String seedUsername,
            @Value("${tms.admin-seed.email}") String seedEmail,
            @Value("${tms.admin-seed.password}") String seedPassword) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordHasher = passwordHasher;
        this.seedUsername = seedUsername;
        this.seedEmail = seedEmail;
        this.seedPassword = seedPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.findByUserName(seedUsername).isPresent()) {
            return; // Already seeded (or an admin renamed/replaced it) - never overwrite.
        }

        Role superAdminRole = roleRepository.findAll().stream()
                .filter(r -> r.name().equals(SUPER_ADMIN_ROLE_NAME))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Super Admin role not found - did the V1 Flyway migration run?"));

        userRepository.save(new User(
                null, seedUsername, "Super Admin", seedEmail, null,
                passwordHasher.hash(seedPassword), true, Instant.now(), null,
                superAdminRole.id(), null, List.of(), null, null, null));

        log.warn(
                "Seeded initial super-admin account (username: {}) - change its password immediately after first login.",
                seedUsername);
    }
}
