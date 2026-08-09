package com.magyen.platform.commercial.presentation.quotation.request;

import java.math.BigDecimal;

/**
 * Payload HTTP para agregar un producto a una cotización.
 */
public record AddQuotationItemRequest(
        String productName,
        int quantity,
        String fabric,
        String color,
        BigDecimal unitPrice,
        ProductSpecificationRequest productSpecification
) {
}
