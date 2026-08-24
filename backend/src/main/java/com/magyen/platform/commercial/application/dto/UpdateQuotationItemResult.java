package com.magyen.platform.commercial.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Resultado del caso de uso de actualizar un producto de una cotización.
 */
public record UpdateQuotationItemResult(
        UUID quotationId,
        UUID itemId,
        BigDecimal totalAmount
) {
}
