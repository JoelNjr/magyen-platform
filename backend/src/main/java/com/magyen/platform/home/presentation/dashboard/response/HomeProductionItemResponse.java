package com.magyen.platform.home.presentation.dashboard.response;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Ítem HTTP de Production Order activa.
 */
public record HomeProductionItemResponse(
        UUID productionOrderId,
        UUID orderId,
        String status,
        LocalDate creationDate,
        String priority
) {
}
