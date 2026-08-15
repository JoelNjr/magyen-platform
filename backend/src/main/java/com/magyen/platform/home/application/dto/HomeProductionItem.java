package com.magyen.platform.home.application.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Production Order activa (CREATED / PLANNED / IN_PROGRESS) en Home.
 */
public record HomeProductionItem(
        UUID productionOrderId,
        UUID orderId,
        String orderNumber,
        UUID customerId,
        String customerName,
        String status,
        LocalDate creationDate,
        String priority
) {
}
