package com.magyen.platform.administration.application.dto;

import com.magyen.platform.administration.domain.AuthenticationRole;

import java.util.UUID;

/**
 * Identidad autenticada expuesta a Presentation.
 */
public record GetAuthenticatedUserResult(
        UUID userId,
        String username,
        AuthenticationRole role,
        boolean enabled
) {
}
