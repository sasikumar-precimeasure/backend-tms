package com.tmsbackend.infrastructure.security;

import com.tmsbackend.domain.port.TokenServicePort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

// Reads the Bearer token, validates it, and - if valid - sets the
// authenticated principal to the user's id (a Long) so controllers can pull
// "who is calling" via SecurityContextHolder without a DB round-trip on
// every request. No roles/authorities are attached here - authorization
// decisions read the caller's actual Permission list from the DB inside
// each use case instead of coarse Spring-Security-role checks, matching the
// existing menu/function permission model.
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final TokenServicePort tokenService;

    public JwtAuthenticationFilter(TokenServicePort tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            tokenService.validateAndGetUserId(token).ifPresent(userId -> {
                var authentication = new UsernamePasswordAuthenticationToken(userId, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        }
        filterChain.doFilter(request, response);
    }
}
