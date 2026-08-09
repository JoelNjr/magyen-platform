package com.magyen.platform.commercial.application.dto;

/**
 * Entrada tipada de una distribución de talla.
 */
public record SizeBreakdownCommand(
        String size,
        int quantity
) {
}
