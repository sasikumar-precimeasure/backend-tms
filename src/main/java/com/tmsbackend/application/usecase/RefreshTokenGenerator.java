package com.tmsbackend.application.usecase;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Component;

// Refresh tokens are high-entropy random opaque strings, not JWTs - stored
// hashed (SHA-256 is fine here, unlike a user password: this is a 256-bit
// random value, not attacker-guessable, so no need for a slow KDF) so a
// leaked database dump doesn't hand out live sessions.
@Component
public class RefreshTokenGenerator {
    private final SecureRandom random = new SecureRandom();

    public String generate() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
