package org.home.paper.server.controller;

import org.home.paper.server.Application;
import org.home.paper.server.dto.JwtAuthenticationResponse;
import org.home.paper.server.dto.SignInRequest;
import org.home.paper.server.dto.SignUpRequest;
import org.home.paper.server.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = CustomerControllerTest.Initializer.class)
@AutoConfigureRestTestClient
public class CustomerControllerTest {

    @Autowired
    private RestTestClient restClient;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void afterEach() {
        userRepository.deleteAll();
    }

    @Test
    public void authorizationTest() {
        var signUpRequest = new SignUpRequest("user", "password");
        restClient.post()
                .uri("/customer/register")
                .body(signUpRequest)
                .exchangeSuccessfully()
                .expectBody(JwtAuthenticationResponse.class)
                .value(jwtAuthentication -> {
                    assertThat(jwtAuthentication).isNotNull();
                    assertThat(jwtAuthentication.token()).isNotNull();
                });

        var signInRequest = new SignInRequest("user", "password");
        restClient.post()
                .uri("/customer/login")
                .body(signInRequest)
                .exchangeSuccessfully()
                .expectBody(JwtAuthenticationResponse.class)
                .value(jwtAuthentication -> {
                    assertThat(jwtAuthentication).isNotNull();
                    assertThat(jwtAuthentication.token()).isNotNull();
                });
    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of().applyTo(configurableApplicationContext.getEnvironment());
        }
    }

}
