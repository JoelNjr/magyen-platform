package com.magyen.platform.production.presentation.productionorder.response;

/**
 * Distribución de talla productiva expuesta por la API de consulta.
 */
public record ProductionSizeBreakdownResponse(
        String size,
        int quantity
) {
}
