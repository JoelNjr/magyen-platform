package com.magyen.platform.administration.application.port;

import com.magyen.platform.administration.domain.AuthenticationRole;

import java.util.UUID;

/**
 * Identidad extraída de un token válido.
 */
public record AuthenticatedPrincipal(
        UUID userId,
        String username,
        AuthenticationRole role
) {
}
