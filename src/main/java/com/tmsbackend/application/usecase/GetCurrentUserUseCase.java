package com.tmsbackend.application.usecase;

import com.tmsbackend.domain.model.User;
import com.tmsbackend.domain.port.UserRepositoryPort;
import org.springframework.stereotype.Component;

@Component
public class GetCurrentUserUseCase {
    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException() {
            super("User not found");
        }
    }

    private final UserRepositoryPort userRepository;

    public GetCurrentUserUseCase(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    public User execute(Long userId) {
        return userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    }
}
