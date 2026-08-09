package com.magyen.platform.commercial.application.dto;

import java.util.List;
import java.util.UUID;

/**
 * Entrada del caso de uso para reemplazar la distribución de tallas de un OrderItem.
 */
public record ReplaceOrderItemSizesCommand(
        UUID orderId,
        UUID orderItemId,
        List<SizeBreakdownCommand> sizes
) {
}
