package com.magyen.platform.commercial.presentation.quotation.response;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Respuesta HTTP tras agregar un producto a una cotización exitosamente.
 */
public record AddQuotationItemResponse(
        UUID quotationId,
        UUID itemId,
        BigDecimal totalAmount
) {
}
