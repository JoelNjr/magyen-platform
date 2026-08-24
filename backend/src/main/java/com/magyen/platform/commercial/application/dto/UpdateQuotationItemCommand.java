package com.magyen.platform.commercial.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entrada del caso de uso para actualizar un producto de una cotización en borrador.
 */
public record UpdateQuotationItemCommand(
        UUID quotationId,
        UUID itemId,
        String productName,
        int quantity,
        String fabric,
        String secondaryFabric,
        String color,
        BigDecimal unitPrice,
        ProductSpecificationCommand productSpecification
) {
}
