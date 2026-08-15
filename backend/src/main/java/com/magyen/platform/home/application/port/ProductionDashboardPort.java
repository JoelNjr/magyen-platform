package com.magyen.platform.home.application.port;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Port de lectura de producción para el Dashboard Home.
 * <p>
 * Resume el ciclo de vida de Production Orders sin recalcular costos.
 */
public interface ProductionDashboardPort {

    HomeProductionSummarySnapshot getCurrentProductionSummary();

    record HomeProductionSummarySnapshot(
            int totalOrders,
            int createdCount,
            int plannedCount,
            int inProgressCount,
            int completedCount,
            List<ProductionDashboardItem> items
    ) {
    }

    /**
     * Órdenes operativas activas: CREATED, PLANNED, IN_PROGRESS.
     */
    record ProductionDashboardItem(
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
}
