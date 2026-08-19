package com.magyen.platform.home.presentation.dashboard.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Ítem HTTP de cuenta por cobrar.
 */
public record HomeReceivableItemResponse(
        UUID orderId,
        String orderNumber,
        String description,
        UUID customerId,
        String customerName,
        BigDecimal orderValue,
        BigDecimal collectedAmount,
        BigDecimal outstandingAmount,
        LocalDate promisedDeliveryDate
) {
}
