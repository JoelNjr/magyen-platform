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
        BigDecimal quantity,
        String unitOfMeasure,
        LocalDateTime consumptionDate,
        String observation
) {
}
