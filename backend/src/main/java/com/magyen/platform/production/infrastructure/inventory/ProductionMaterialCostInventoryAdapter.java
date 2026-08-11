package com.magyen.platform.production.infrastructure.inventory;

import com.magyen.platform.inventory.application.dto.GetInventoryMovementBySourceQuery;
import com.magyen.platform.inventory.application.usecase.GetInventoryMovementBySourceUseCase;
import com.magyen.platform.inventory.domain.InventoryMovementSourceType;
import com.magyen.platform.production.application.port.ProductionMaterialCostInventoryPort;
import com.magyen.platform.production.application.port.ProductionMaterialHistoricalCost;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador que obtiene el costo histórico de un consumo desde Inventory.
 */
public class ProductionMaterialCostInventoryAdapter implements ProductionMaterialCostInventoryPort {

    private final GetInventoryMovementBySourceUseCase getInventoryMovementBySourceUseCase;

    public ProductionMaterialCostInventoryAdapter(
            GetInventoryMovementBySourceUseCase getInventoryMovementBySourceUseCase
    ) {
        this.getInventoryMovementBySourceUseCase = Objects.requireNonNull(
                getInventoryMovementBySourceUseCase,
                "Get inventory movement by source use case must not be null"
        );
    }

    @Override
    public Optional<ProductionMaterialHistoricalCost> findHistoricalCost(UUID productionMaterialConsumptionId) {
        Objects.requireNonNull(productionMaterialConsumptionId, "Production material consumption id must not be null");

        return getInventoryMovementBySourceUseCase.execute(
                        new GetInventoryMovementBySourceQuery(
                                InventoryMovementSourceType.PRODUCTION,
                                productionMaterialConsumptionId
                        )
                )
                .map(movement -> new ProductionMaterialHistoricalCost(
                        movement.unitCost(),
                        movement.totalCost()
                ));
    }
}
