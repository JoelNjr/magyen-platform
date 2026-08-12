package com.magyen.platform.home.presentation.dashboard.response;

import java.util.List;

/**
 * Sección HTTP de alertas de stock bajo (inventario general).
 */
public record HomeInventoryAlertsResponse(
        int lowStockCount,
        List<HomeInventoryAlertItemResponse> items
) {
}
