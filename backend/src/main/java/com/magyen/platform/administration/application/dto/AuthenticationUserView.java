package com.magyen.platform.administration.application.dto;

import com.magyen.platform.administration.domain.AuthenticationRole;

import java.util.UUID;

/**
 * Vista de administración de un usuario interno. Nunca incluye hash ni password.
 */
public record AuthenticationUserView(
        UUID id,
        String username,
        AuthenticationRole role,
        boolean enabled
) {
}
