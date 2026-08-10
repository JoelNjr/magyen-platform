package com.magyen.platform.production.presentation.productionorder.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Respuesta HTTP de un consumo de material de producción.
 */
public record GetProductionMaterialConsumptionResponse(
        UUID consumptionId,
        UUID productionOrderId,
        UUID inventoryItemId,
        BigDecimal quantity,
        String unitOfMeasure,
        LocalDateTime consumptionDate,
        String observation
) {
}
