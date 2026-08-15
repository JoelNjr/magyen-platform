package com.magyen.platform.administration.presentation.user.request;

public record CreateAuthenticationUserRequest(
        String username,
        String password,
        String role
) {
}
