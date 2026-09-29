package com.tmsbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

// UserDetailsServiceAutoConfiguration is excluded because this app never
// uses Spring Security's UserDetailsService mechanism at all - auth is
// entirely JWT + the domain's own PasswordHasherPort/UserRepositoryPort
// (see LoginUseCase). Without this exclusion, Boot generates a random
// in-memory user/password on every startup and logs it, which is pure
// noise here and could be mistaken for a real credential.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class TmsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TmsBackendApplication.class, args);
    }

}
