package com.magyen.platform.commercial.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Resultado del caso de uso de eliminar un producto de una cotización.
 */
public record RemoveQuotationItemResult(
        UUID quotationId,
        BigDecimal totalAmount
) {
}
