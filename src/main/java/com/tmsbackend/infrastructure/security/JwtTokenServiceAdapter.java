package com.tmsbackend.infrastructure.security;

import com.tmsbackend.domain.model.User;
import com.tmsbackend.domain.port.TokenServicePort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// Access tokens are short-lived, stateless JWTs (HS256) - the long-lived,
// revocable half of the session lives in refresh_tokens (see
// RefreshTokenRepositoryAdapter), not here.
@Component
public class JwtTokenServiceAdapter implements TokenServicePort {
    private final Key signingKey;
    private final long accessTokenTtlSeconds;

    public JwtTokenServiceAdapter(
            @Value("${tms.jwt.secret}") String secret,
            @Value("${tms.jwt.access-token-ttl-seconds}") long accessTokenTtlSeconds) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
    }

    @Override
    public IssuedAccessToken issueAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(Duration.ofSeconds(accessTokenTtlSeconds));

        String token = Jwts.builder()
                .subject(String.valueOf(user.id()))
                .claim("userName", user.userName())
                .claim("roleId", user.roleId())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();

        return new IssuedAccessToken(token, accessTokenTtlSeconds);
    }

    @Override
    public Optional<Long> validateAndGetUserId(String accessToken) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith((javax.crypto.SecretKey) signingKey)
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();
            return Optional.of(Long.valueOf(claims.getSubject()));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
