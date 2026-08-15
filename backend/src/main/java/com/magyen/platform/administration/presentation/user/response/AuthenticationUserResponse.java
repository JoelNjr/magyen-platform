package com.magyen.platform.administration.presentation.user.response;

public record AuthenticationUserResponse(
        String id,
        String username,
        String role,
        boolean enabled
) {
}
