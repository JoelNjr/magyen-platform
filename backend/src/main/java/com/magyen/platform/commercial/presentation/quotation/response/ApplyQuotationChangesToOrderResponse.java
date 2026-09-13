package com.magyen.platform.commercial.presentation.quotation.response;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Resultado HTTP de aplicar la cotización persistida a su Orden.
 */
public record ApplyQuotationChangesToOrderResponse(
        UUID orderId,
        BigDecimal subtotalAmount,
        BigDecimal discountAmount,
        BigDecimal totalAmount
) {
}
