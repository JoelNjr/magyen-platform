package com.magyen.platform.production.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Resultado del registro de un consumo de material en producción.
 */
public record RegisterProductionMaterialConsumptionResult(
        UUID consumptionId,
        UUID productionOrderId,
        UUID inventoryItemId,
        String materialName,
        String materialCode,
        BigDecimal quantity,
        String unitOfMeasure,
        BigDecimal unitCost,
        BigDecimal totalCost,
        BigDecimal remainingStock,
        LocalDateTime consumptionDate,
        String observation
) {
}
