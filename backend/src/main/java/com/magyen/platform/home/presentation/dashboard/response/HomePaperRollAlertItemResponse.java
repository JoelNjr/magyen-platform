package com.magyen.platform.home.presentation.dashboard.response;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Ítem HTTP de alerta de rollo de papel.
 */
public record HomePaperRollAlertItemResponse(
        UUID inventoryItemId,
        String materialCode,
        String name,
        String paperRollNumber,
        BigDecimal stock,
        String unitOfMeasure,
        BigDecimal minimumStock,
        boolean lowStock
) {
}
