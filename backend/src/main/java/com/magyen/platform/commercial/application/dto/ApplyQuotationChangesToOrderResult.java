package com.magyen.platform.commercial.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Resultado comercial de aplicar una cotización a su Orden.
 */
public record ApplyQuotationChangesToOrderResult(
        UUID orderId,
        BigDecimal subtotalAmount,
        BigDecimal discountAmount,
        BigDecimal totalAmount
) {
}
