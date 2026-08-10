package com.magyen.platform.production.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entrada del caso de uso para registrar un consumo de material en producción.
 */
public record RegisterProductionMaterialConsumptionCommand(
        UUID productionOrderId,
        UUID inventoryItemId,
        BigDecimal quantity,
        String unitOfMeasure,
        String observation
) {
}
