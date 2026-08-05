package com.magyen.platform.commercial.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entrada del caso de uso para agregar un producto a una cotización.
 */
public record AddQuotationItemCommand(
        UUID quotationId,
        String productName,
        int quantity,
        String fabric,
        String color,
        BigDecimal unitPrice
) {
}
