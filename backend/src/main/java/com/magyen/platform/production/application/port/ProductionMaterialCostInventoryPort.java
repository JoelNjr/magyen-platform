package com.magyen.platform.production.application.port;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de lectura de Production hacia Inventory para atribución de costo histórico.
 * <p>
 * El costo proviene del movimiento OUT con {@code sourceType = PRODUCTION}
 * y {@code sourceId = productionMaterialConsumptionId}.
 * No lee el unitCost actual del ítem de inventario.
 */
public interface ProductionMaterialCostInventoryPort {

    /**
     * Obtiene el snapshot histórico de costo del consumo, si existe movimiento.
     * Si no hay movimiento o el movimiento no tiene valoración, el Optional vacío
     * o los costos null se tratan como consumo sin valorizar.
     */
    Optional<ProductionMaterialHistoricalCost> findHistoricalCost(UUID productionMaterialConsumptionId);
}
