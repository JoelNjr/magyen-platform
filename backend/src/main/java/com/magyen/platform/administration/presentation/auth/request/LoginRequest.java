package com.magyen.platform.administration.presentation.auth.request;

/**
 * Payload HTTP para autenticar una identidad.
 */
public record LoginRequest(
        String username,
        String password
) {
}
