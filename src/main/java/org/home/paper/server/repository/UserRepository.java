package org.home.paper.server.repository;

import org.home.paper.server.model.User;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);
}
