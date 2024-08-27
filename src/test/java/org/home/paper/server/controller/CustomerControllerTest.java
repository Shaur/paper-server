package org.home.paper.server.controller;

import org.home.paper.server.Application;
import org.home.paper.server.dto.JwtAuthenticationResponse;
import org.home.paper.server.dto.SignInRequest;
import org.home.paper.server.dto.SignUpRequest;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = CustomerControllerTest.Initializer.class)
public class CustomerControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void authorizationTest() {
        var signUpRequest = new SignUpRequest("user", "password");
        var signUpResponse = restTemplate.postForEntity("/customer/register", signUpRequest, JwtAuthenticationResponse.class);
        var jwtAuthentication = signUpResponse.getBody();

        assertThat(jwtAuthentication).isNotNull();
        assertThat(jwtAuthentication.token()).isNotNull();

        var signInRequest = new SignInRequest("user", "password");
        var signInResponse = restTemplate.postForEntity("/customer/login", signInRequest, JwtAuthenticationResponse.class);

        assertThat(signInResponse.getBody()).isNotNull();
        assertThat(signInResponse.getBody().token()).isNotNull();
    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of().applyTo(configurableApplicationContext.getEnvironment());
        }
    }

}
