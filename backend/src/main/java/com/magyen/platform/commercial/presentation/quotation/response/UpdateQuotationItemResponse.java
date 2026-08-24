package com.magyen.platform.commercial.presentation.quotation.response;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Respuesta HTTP tras actualizar un producto de una cotización.
 */
public record UpdateQuotationItemResponse(
        UUID quotationId,
        UUID itemId,
        BigDecimal totalAmount
) {
}
