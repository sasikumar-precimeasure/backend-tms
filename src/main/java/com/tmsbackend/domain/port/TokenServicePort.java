package com.tmsbackend.domain.port;

import com.tmsbackend.domain.model.User;

public interface TokenServicePort {
    record IssuedAccessToken(String token, long expiresInSeconds) {
    }

    IssuedAccessToken issueAccessToken(User user);

    // Returns the subject (user id) if the token is valid, else empty.
    java.util.Optional<Long> validateAndGetUserId(String accessToken);
}
