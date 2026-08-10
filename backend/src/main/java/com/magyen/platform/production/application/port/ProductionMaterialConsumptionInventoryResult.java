package com.magyen.platform.production.application.port;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Resultado del consumo físico en Inventory solicitado desde Production.
 */
public record ProductionMaterialConsumptionInventoryResult(
        UUID movementId,
        UUID inventoryItemId,
        BigDecimal resultingStock,
        BigDecimal unitCost,
        BigDecimal totalCost,
        boolean alreadyProcessed
) {
}
