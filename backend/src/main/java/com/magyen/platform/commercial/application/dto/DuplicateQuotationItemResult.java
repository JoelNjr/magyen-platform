package com.magyen.platform.commercial.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Ítem nuevo creado por duplicación y totales recalculados de la cotización.
 */
public record DuplicateQuotationItemResult(
        UUID quotationId,
        UUID sourceItemId,
        UUID itemId,
        BigDecimal subtotalAmount,
        BigDecimal discountAmount,
        BigDecimal totalAmount
) {
}
