package com.magyen.platform.plotter.application.port;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Resultado del consumo de papel solicitado por Plotter a Inventory.
 */
public record PlotterJobInventoryConsumeResult(
        UUID movementId,
        UUID inventoryItemId,
        BigDecimal resultingStock,
        BigDecimal unitCost,
        BigDecimal totalCost,
        boolean alreadyProcessed
) {
}
