package org.home.paper.server.controller;

import org.home.paper.server.dto.JwtAuthenticationResponse;
import org.home.paper.server.dto.SignInRequest;
import org.home.paper.server.dto.SignUpRequest;
import org.home.paper.server.service.AuthenticationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customer")
public class CustomerController {

    private final AuthenticationService service;

    public CustomerController(AuthenticationService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public JwtAuthenticationResponse login(@RequestBody SignInRequest request) {
        return service.signIn(request);
    }

    @PostMapping("/register")
    public JwtAuthenticationResponse register(@RequestBody SignUpRequest request) {
        return service.singUp(request);
    }


}
