package com.magyen.platform.production.application.port;

import java.math.BigDecimal;

/**
 * Snapshot histórico de costo de material asociado a un consumo de producción.
 * <p>
 * Ambos valores pueden ser null cuando el OUT de Inventory no congeló valoración.
 */
public record ProductionMaterialHistoricalCost(
        BigDecimal unitCost,
        BigDecimal totalCost
) {
}
