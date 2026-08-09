package com.magyen.platform.commercial.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Representación de un producto de cotización para casos de uso de consulta.
 */
public record QuotationItemResult(
        UUID itemId,
        String productName,
        int quantity,
        String fabric,
        String color,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        ProductSpecificationResult productSpecification
) {
}
