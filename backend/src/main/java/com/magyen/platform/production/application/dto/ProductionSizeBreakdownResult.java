package com.magyen.platform.production.application.dto;

/**
 * Representación de una distribución de talla productiva para casos de uso de consulta.
 */
public record ProductionSizeBreakdownResult(
        String size,
        int quantity
) {
}
