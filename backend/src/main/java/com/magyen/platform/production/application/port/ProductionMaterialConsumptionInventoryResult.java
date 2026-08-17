package com.magyen.platform.production.application.port;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Resultado del consumo físico en Inventory solicitado desde Production.
 */
public record ProductionMaterialConsumptionInventoryResult(
        UUID movementId,
        UUID inventoryItemId,
        String materialName,
        String materialCode,
        BigDecimal resultingStock,
        BigDecimal unitCost,
        BigDecimal totalCost,
        boolean alreadyProcessed
) {
}
