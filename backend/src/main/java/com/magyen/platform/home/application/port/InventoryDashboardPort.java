package com.magyen.platform.home.application.port;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Port de lectura de inventario para el Dashboard Home.
 * <p>
 * Reutiliza la semántica de stock bajo y rollos de papel de Inventory.
 * No recalcula {@code stock <= minimumStock} ni la clasificación de rollos.
 */
public interface InventoryDashboardPort {

    /**
     * Alertas actuales de inventario y de rollos de papel (sin filtro de período).
     */
    HomeInventoryAlertsSnapshot getCurrentInventoryAlerts();

    /**
     * Snapshot combinado: alertas generales + alertas de rollos Plotter.
     */
    record HomeInventoryAlertsSnapshot(
            InventoryAlertsSection inventoryAlerts,
            PaperRollAlertsSection paperRollAlerts
    ) {
    }

    record InventoryAlertsSection(
            int lowStockCount,
            List<InventoryAlertItem> items
    ) {
    }

    record PaperRollAlertsSection(
            int lowStockCount,
            List<PaperRollAlertItem> items
    ) {
    }

    /**
     * Material con stock bajo (cualquier tipo monitoreado).
     */
    record InventoryAlertItem(
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

    /**
     * Rollo de papel Plotter con stock bajo
     * ({@code PAPER} + {@code METER} + {@code paperRollNumber}).
     */
    record PaperRollAlertItem(
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
}
