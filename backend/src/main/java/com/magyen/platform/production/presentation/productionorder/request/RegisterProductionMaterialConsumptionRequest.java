package com.magyen.platform.production.presentation.productionorder.request;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Payload HTTP para registrar un consumo de material en producción.
 */
public record RegisterProductionMaterialConsumptionRequest(
        UUID inventoryItemId,
        BigDecimal quantity,
        String unitOfMeasure,
        String observation
) {
}
