package com.magyen.platform.administration.application.port;

import java.time.Instant;

/**
 * Token de autenticación emitido por el adaptador de infraestructura.
 */
public record IssuedAuthenticationToken(
        String token,
        Instant expiresAt,
        long expiresInSeconds
) {
}
