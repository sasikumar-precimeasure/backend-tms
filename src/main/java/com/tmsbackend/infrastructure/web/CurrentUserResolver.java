package com.tmsbackend.infrastructure.web;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

// JwtAuthenticationFilter sets the authenticated principal to the caller's
// user id (a Long) directly - this just reads it back out for controllers.
@Component
public class CurrentUserResolver {
    public Long requireUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Long userId) {
            return userId;
        }
        throw new IllegalStateException("No authenticated user in security context");
    }
}
