package com.magyen.platform.home.presentation.dashboard.response;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Ítem HTTP de alerta de stock bajo.
 */
public record HomeInventoryAlertItemResponse(
        UUID inventoryItemId,
        String materialCode,
        String name,
        String description,
        String materialType,
        String paperRollNumber,
        BigDecimal stock,
        String unitOfMeasure,
        BigDecimal minimumStock,
        boolean lowStock
) {
}
