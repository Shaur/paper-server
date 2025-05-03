package org.home.paper.server.model.auth;

import jakarta.persistence.*;

import java.util.List;

@Entity(name = "user_data")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String password;

    private String role;

    public User(Long id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public User() {
    }

    public Long getId() {
        return id;
    }

    public String getRole() {
        return role;
    }

    public String getUsername() {
        return username;
    }

    public AuthentificationEntity toSecure() {
        return new AuthentificationEntity(id, username, password, List.of());
    }
}
