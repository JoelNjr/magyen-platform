package com.magyen.platform.commercial.application.dto;

/**
 * Representación de una distribución de talla para casos de uso de consulta.
 */
public record SizeBreakdownResult(
        String size,
        int quantity
) {
}
