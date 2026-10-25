package com.magyen.platform.commercial.presentation.quotation.response;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Ítem duplicado y totales recalculados de la cotización.
 */
public record DuplicateQuotationItemResponse(
        UUID quotationId,
        UUID sourceItemId,
        UUID itemId,
        BigDecimal subtotalAmount,
        BigDecimal discountAmount,
        BigDecimal totalAmount
) {
}
