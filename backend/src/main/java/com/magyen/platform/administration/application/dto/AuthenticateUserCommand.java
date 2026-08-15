package com.magyen.platform.administration.application.dto;

/**
 * Entrada del caso de uso de autenticación.
 */
public record AuthenticateUserCommand(
        String username,
        String password
) {
}
