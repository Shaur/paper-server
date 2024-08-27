package org.home.paper.server.service;

import org.home.paper.server.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    User save(User user);

    User create(User user);

    User getByUsername(String username);

    User getCurrentUser();

}
