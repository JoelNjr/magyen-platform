package com.magyen.platform.commercial.application.port;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Port de lectura de costos de producción atribuibles a una Orden comercial.
 * <p>
 * Siempre retorna un snapshot; {@code productionOrderFound} indica si existe
 * Orden de Producción (como máximo una por orderId).
 */
public interface ProductionOrderCostPort {

    ProductionOrderCostSnapshot findCostsByOrderId(UUID orderId);

    record ProductionOrderCostSnapshot(
            boolean productionOrderFound,
            UUID productionOrderId,
            BigDecimal materialCost,
            int materialConsumptionCount,
            int valuedMaterialConsumptionCount,
            int unvaluedMaterialConsumptionCount,
            BigDecimal laborCost,
            int laborWorkCount
    ) {
    }
}
