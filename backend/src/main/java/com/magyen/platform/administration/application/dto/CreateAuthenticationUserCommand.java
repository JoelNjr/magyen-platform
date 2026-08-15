package com.magyen.platform.administration.application.dto;

import com.magyen.platform.administration.domain.AuthenticationRole;

public record CreateAuthenticationUserCommand(
        String username,
        String password,
        AuthenticationRole role
) {
}
