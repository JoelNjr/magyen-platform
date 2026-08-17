package com.magyen.platform.production.presentation.operator.request;

/**
 * Payload HTTP para crear un operario de producción.
 */
public record CreateProductionOperatorRequest(
        String name
) {
}
