package com.magyen.platform.production.application.dto;

/**
 * Entrada del caso de uso para crear un operario de producción.
 */
public record CreateProductionOperatorCommand(
        String name
) {
}
