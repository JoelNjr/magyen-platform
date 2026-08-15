package com.magyen.platform.administration.presentation.auth.response;

import java.util.UUID;

/**
 * Respuesta HTTP de autenticación exitosa.
 */
public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        UUID userId,
        String username,
        String role
) {
}
