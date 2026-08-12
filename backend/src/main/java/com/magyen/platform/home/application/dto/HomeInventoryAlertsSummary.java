package com.magyen.platform.home.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Sección de alertas de stock bajo del Dashboard Home.
 */
public record HomeInventoryAlertsSummary(
        int lowStockCount,
        List<HomeInventoryAlertItem> items
) {
}
