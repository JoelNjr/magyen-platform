package com.magyen.platform.administration.application.dto;

import com.magyen.platform.administration.domain.AuthenticationRole;

import java.util.UUID;

/**
 * Resultado del caso de uso de autenticación.
 */
public record AuthenticateUserResult(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        UUID userId,
        String username,
        AuthenticationRole role
) {
}
