package com.magyen.platform.home.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Material con stock bajo en el Dashboard Home.
 */
public record HomeInventoryAlertItem(
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
