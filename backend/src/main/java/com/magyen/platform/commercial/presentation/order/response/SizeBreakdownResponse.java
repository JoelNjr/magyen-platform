package com.magyen.platform.commercial.presentation.order.response;

/**
 * Distribución de talla expuesta por la API de órdenes.
 */
public record SizeBreakdownResponse(
        String size,
        int quantity
) {
}
