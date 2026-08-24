package com.magyen.platform.commercial.presentation.quotation.response;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Respuesta HTTP tras eliminar un producto de una cotización.
 */
public record RemoveQuotationItemResponse(
        UUID quotationId,
        BigDecimal totalAmount
) {
}
