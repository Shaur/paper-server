package org.home.paper.server.dto;

public record SignInRequest(
        String username,
        String password
) {
}
