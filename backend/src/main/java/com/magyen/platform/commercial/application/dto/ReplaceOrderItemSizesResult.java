package com.magyen.platform.commercial.application.dto;

import java.util.List;
import java.util.UUID;

/**
 * Resultado del caso de uso que reemplaza la distribución de tallas de un OrderItem.
 */
public record ReplaceOrderItemSizesResult(
        UUID orderItemId,
        List<SizeBreakdownResult> sizes
) {
}
