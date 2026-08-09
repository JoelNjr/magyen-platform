package com.magyen.platform.commercial.presentation.order.response;

import java.util.List;
import java.util.UUID;

/**
 * Respuesta HTTP tras reemplazar la distribución de tallas de un OrderItem.
 */
public record ReplaceOrderItemSizesResponse(
        UUID orderItemId,
        List<SizeBreakdownResponse> sizes
) {
}
