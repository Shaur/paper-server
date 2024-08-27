package org.home.paper.server.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.home.paper.server.model.User;
import org.home.paper.server.repository.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class DefaultUserRepository implements UserRepository {

    private final JdbcTemplate template;
    private final EntityManager entityManager;

    private static final Map<String, User> users = new HashMap<>();

    public DefaultUserRepository(JdbcTemplate jdbcTemplate, EntityManager entityManager) {
        this.template = jdbcTemplate;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public User save(User user) {
        return entityManager.merge(user);
    }

    @Override
    public boolean existsByUsername(String username) {
        var count = template.queryForObject(
                "select count(*) from user_data where username = ?",
                Integer.class,
                username
        );

        return count != null && count > 0;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        var rows = template.queryForList("select * from user_data where username = ?", username);

        if (rows.isEmpty()) return Optional.empty();

        var row = rows.getFirst();
        var user = new User(
                (Long) row.get("id"),
                (String) row.get("username"),
                (String) row.get("password")
        );

        return Optional.of(user);
    }
}
