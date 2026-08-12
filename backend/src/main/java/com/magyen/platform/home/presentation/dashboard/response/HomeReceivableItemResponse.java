package com.magyen.platform.home.presentation.dashboard.response;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Ítem HTTP de cuenta por cobrar.
 */
public record HomeReceivableItemResponse(
        UUID orderId,
        String orderNumber,
        UUID customerId,
        BigDecimal orderValue,
        BigDecimal collectedAmount,
        BigDecimal outstandingAmount
) {
}
