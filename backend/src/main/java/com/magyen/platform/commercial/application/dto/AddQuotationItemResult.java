package com.magyen.platform.commercial.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Resultado del caso de uso de agregar un producto a una cotización.
 */
public record AddQuotationItemResult(
        UUID quotationId,
        UUID itemId,
        BigDecimal totalAmount
) {
}
