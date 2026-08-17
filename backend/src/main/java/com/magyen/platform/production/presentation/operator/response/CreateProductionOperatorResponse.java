package com.magyen.platform.production.presentation.operator.response;

import java.util.UUID;

/**
 * Respuesta HTTP de creación de un operario de producción.
 */
public record CreateProductionOperatorResponse(
        UUID operatorId,
        String name,
        boolean active
) {
}
