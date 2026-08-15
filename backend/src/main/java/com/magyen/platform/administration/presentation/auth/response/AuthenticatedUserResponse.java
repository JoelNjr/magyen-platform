package com.magyen.platform.administration.presentation.auth.response;

import java.util.UUID;

/**
 * Identidad autenticada sin secretos.
 */
public record AuthenticatedUserResponse(
        UUID userId,
        String username,
        String role,
        boolean enabled
) {
}
