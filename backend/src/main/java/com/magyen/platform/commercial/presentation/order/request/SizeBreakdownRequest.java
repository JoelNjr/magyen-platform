package com.magyen.platform.commercial.presentation.order.request;

/**
 * Payload HTTP de una distribución de talla.
 */
public record SizeBreakdownRequest(
        String size,
        int quantity
) {
}
