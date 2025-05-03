package org.home.paper.server.service.impl;

import org.home.paper.server.exceptions.ObjectNotFoundException;
import org.home.paper.server.model.auth.User;
import org.home.paper.server.repository.RoleRepository;
import org.home.paper.server.repository.UserRepository;
import org.home.paper.server.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DefaultUserService implements UserService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    public DefaultUserService(
            UserRepository userRepository,
            RoleRepository roleRepository
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public User create(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        return save(user);
    }

    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = getByUsername(username);
        var role = roleRepository.findByName(user.getRole());
        if (role == null) {
            throw new ObjectNotFoundException("Role", user.getRole());
        }

        return user.toSecure().withPrivileges(role.getPrivileges());
    }
}
