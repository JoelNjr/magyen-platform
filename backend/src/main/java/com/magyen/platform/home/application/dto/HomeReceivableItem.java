package com.magyen.platform.home.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Ítem de cuenta por cobrar en el Dashboard Home.
 */
public record HomeReceivableItem(
        UUID orderId,
        String orderNumber,
        String description,
        UUID customerId,
        String customerName,
        BigDecimal orderValue,
        BigDecimal collectedAmount,
        BigDecimal outstandingAmount
) {
}
