package com.magyen.platform.production.application.port;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Puerto de salida de Production hacia Inventory para consumir material físico.
 * <p>
 * Production no conoce persistencia Inventory; solo solicita el descuento de stock
 * asociado a un {@code productionMaterialConsumptionId}.
 */
public interface ProductionMaterialConsumptionInventoryPort {

    /**
     * Descuenta stock mediante un OUT de Inventory con
     * {@code sourceType = PRODUCTION} y {@code sourceId = productionMaterialConsumptionId}.
     * <p>
     * Idempotente: reintentos del mismo consumptionId no vuelven a descontar stock.
     */
    ProductionMaterialConsumptionInventoryResult consumeMaterial(
            UUID inventoryItemId,
            BigDecimal quantity,
            String unitOfMeasure,
            UUID productionMaterialConsumptionId,
            String observation
    );
}
