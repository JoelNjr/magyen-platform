package com.magyen.platform.commercial.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Línea de comisión de un pedido dentro de un mes de confirmación.
 */
public record SellerCommissionOrderLine(
        UUID orderId,
        String orderNumber,
        String customerName,
        LocalDate confirmationDate,
        BigDecimal orderTotal,
        BigDecimal commissionRate,
        BigDecimal commissionAmount
) {
}
