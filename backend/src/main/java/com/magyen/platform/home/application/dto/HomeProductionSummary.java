package com.magyen.platform.home.application.dto;

import java.util.List;

/**
 * Resumen operativo de Production Orders en el Dashboard Home.
 */
public record HomeProductionSummary(
        int totalOrders,
        int createdCount,
        int plannedCount,
        int inProgressCount,
        int completedCount,
        List<HomeProductionItem> items
) {
}
