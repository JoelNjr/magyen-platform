package com.magyen.platform.production.presentation.productionorder.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Respuesta HTTP tras registrar un consumo de material en producción.
 */
public record RegisterProductionMaterialConsumptionResponse(
        UUID consumptionId,
        UUID productionOrderId,
        UUID inventoryItemId,
        BigDecimal quantity,
        String unitOfMeasure,
        LocalDateTime consumptionDate,
        String observation
) {
}
