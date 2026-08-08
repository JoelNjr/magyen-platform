package com.magyen.platform.commercial.presentation.quotation.response;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Producto de cotización expuesto por la API de detalle.
 */
public record QuotationItemResponse(
        UUID itemId,
        String productName,
        int quantity,
        String fabric,
        String color,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}
