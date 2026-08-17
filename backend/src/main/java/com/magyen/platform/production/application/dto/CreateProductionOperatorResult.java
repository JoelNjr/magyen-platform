package com.magyen.platform.production.application.dto;

import java.util.UUID;

/**
 * Resultado de crear un operario de producción.
 */
public record CreateProductionOperatorResult(
        UUID operatorId,
        String name,
        boolean active
) {
}
