package com.magyen.platform.administration.application.dto;

import com.magyen.platform.administration.domain.AuthenticationRole;

import java.util.UUID;

public record ChangeAuthenticationUserRoleCommand(
        UUID userId,
        AuthenticationRole role
) {
}
