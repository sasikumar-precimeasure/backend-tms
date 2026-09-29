package com.tmsbackend.domain.port;

import com.tmsbackend.domain.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {
    Optional<User> findById(Long id);

    Optional<User> findByUserName(String userName);

    Optional<User> findByEmail(String email);

    List<User> findAll();

    User save(User user);

    void updateLastLoginAt(Long userId, java.time.Instant when);

    void deleteById(Long id);

    long countByRoleId(Long roleId);
}
