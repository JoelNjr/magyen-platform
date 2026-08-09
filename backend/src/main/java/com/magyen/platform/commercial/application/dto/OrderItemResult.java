package com.magyen.platform.commercial.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Representación de un producto comprometido de una Orden para casos de uso de consulta.
 */
public record OrderItemResult(
        UUID itemId,
        String productName,
        int quantity,
        String fabric,
        String color,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        ProductSpecificationResult productSpecification,
        List<SizeBreakdownResult> sizes
) {
}
