package com.magyen.platform.commercial.presentation.order.request;

import java.util.List;

/**
 * Payload HTTP para reemplazar la distribución completa de tallas de un OrderItem.
 */
public record ReplaceOrderItemSizesRequest(
        List<SizeBreakdownRequest> sizes
) {
}
