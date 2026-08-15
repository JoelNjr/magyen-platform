package com.magyen.platform.administration.application.dto;

import java.util.UUID;

/**
 * Consulta de identidad autenticada.
 */
public record GetAuthenticatedUserQuery(
        UUID userId
) {
}
