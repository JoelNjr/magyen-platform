package com.magyen.platform.home.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Rollo de papel Plotter con stock bajo en el Dashboard Home.
 */
public record HomePaperRollAlertItem(
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
