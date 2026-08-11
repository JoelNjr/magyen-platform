package com.magyen.platform.production.presentation.productionorder.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Respuesta HTTP de un consumo de material de producción.
 * <p>
 * {@code unitCost} y {@code totalCost} son el snapshot histórico de Inventory;
 * pueden ser null cuando el consumo no tiene costo configurado.
 */
public record GetProductionMaterialConsumptionResponse(
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
