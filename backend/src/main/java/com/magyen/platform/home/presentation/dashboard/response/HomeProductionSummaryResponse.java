package com.magyen.platform.home.presentation.dashboard.response;

import java.util.List;

/**
 * Sección HTTP de resumen de Production Orders.
 */
public record HomeProductionSummaryResponse(
        int totalOrders,
        int createdCount,
        int plannedCount,
        int inProgressCount,
        int completedCount,
        List<HomeProductionItemResponse> items
) {
}
