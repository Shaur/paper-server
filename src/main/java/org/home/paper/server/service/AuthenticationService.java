package org.home.paper.server.service;

import jakarta.annotation.PostConstruct;
import org.home.paper.server.dto.JwtAuthenticationResponse;
import org.home.paper.server.dto.SignInRequest;
import org.home.paper.server.dto.SignUpRequest;
import org.home.paper.server.model.auth.RoleType;
import org.home.paper.server.model.auth.User;
import org.home.paper.server.model.auth.Role;
import org.home.paper.server.repository.RoleRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuthenticationService {

    private final UserService userService;

    private final JwtService jwtService;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final RoleRepository roleRepository;

    private Map<String, Role> roles = new HashMap<>();

    @PostConstruct
    public void initRoles() {
        roles = roleRepository.findAll().stream().collect(Collectors.toMap(Role::getName, role -> role));
    }

    public AuthenticationService(
            UserService userService,
            JwtService jwtService,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            RoleRepository roleRepository
    ) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.roleRepository = roleRepository;
    }

    public JwtAuthenticationResponse singUp(SignUpRequest request) {
        var user = new User(
                null,
                request.username(),
                passwordEncoder.encode(request.password()),
                RoleType.USER.getLabel()
        );

        var privileges = roles.get(user.getRole()).getPrivileges();
        var entity = userService.create(user).toSecure().withPrivileges(privileges);

        var jwt = jwtService.generateToken(entity);
        return new JwtAuthenticationResponse(jwt);
    }

    public JwtAuthenticationResponse signIn(SignInRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        var user = userService.loadUserByUsername(request.username());


        var jwt = jwtService.generateToken(user);
        return new JwtAuthenticationResponse(jwt);
    }
}
