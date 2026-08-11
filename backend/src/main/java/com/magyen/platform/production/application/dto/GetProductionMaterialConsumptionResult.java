package com.magyen.platform.production.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Read model de un consumo de material de producción.
 * <p>
 * {@code unitCost} y {@code totalCost} provienen del snapshot histórico del OUT
 * de Inventory. Son null cuando el consumo no tiene valoración conocida.
 */
public record GetProductionMaterialConsumptionResult(
        UUID consumptionId,
        UUID productionOrderId,
        UUID inventoryItemId,
        BigDecimal quantity,
        String unitOfMeasure,
        LocalDateTime consumptionDate,
        String observation,
        BigDecimal unitCost,
        BigDecimal totalCost
) {
}
