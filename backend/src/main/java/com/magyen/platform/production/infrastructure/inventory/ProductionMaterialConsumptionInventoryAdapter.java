package com.magyen.platform.production.infrastructure.inventory;

import com.magyen.platform.inventory.application.dto.ConsumeInventoryMaterialCommand;
import com.magyen.platform.inventory.application.dto.ConsumeInventoryMaterialResult;
import com.magyen.platform.inventory.application.dto.GetInventoryItemQuery;
import com.magyen.platform.inventory.application.dto.GetInventoryItemResult;
import com.magyen.platform.inventory.application.usecase.ConsumeInventoryMaterialUseCase;
import com.magyen.platform.inventory.application.usecase.GetInventoryItemUseCase;
import com.magyen.platform.inventory.domain.InventoryMovementSourceType;
import com.magyen.platform.production.application.port.ProductionMaterialConsumptionInventoryPort;
import com.magyen.platform.production.application.port.ProductionMaterialConsumptionInventoryResult;
import com.magyen.platform.production.domain.exception.ProductionDomainException;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Adaptador que traduce el puerto de Production a la capacidad de consumo de Inventory.
 */
public class ProductionMaterialConsumptionInventoryAdapter implements ProductionMaterialConsumptionInventoryPort {

    private final ConsumeInventoryMaterialUseCase consumeInventoryMaterialUseCase;
    private final GetInventoryItemUseCase getInventoryItemUseCase;

    public ProductionMaterialConsumptionInventoryAdapter(
            ConsumeInventoryMaterialUseCase consumeInventoryMaterialUseCase,
            GetInventoryItemUseCase getInventoryItemUseCase
    ) {
        this.consumeInventoryMaterialUseCase = Objects.requireNonNull(
                consumeInventoryMaterialUseCase,
                "Consume inventory material use case must not be null"
        );
        this.getInventoryItemUseCase = Objects.requireNonNull(
                getInventoryItemUseCase,
                "Get inventory item use case must not be null"
        );
    }

    @Override
    public void requireConsumableForProduction(UUID inventoryItemId) {
        Objects.requireNonNull(inventoryItemId, "Inventory item id must not be null");
        GetInventoryItemResult item = getInventoryItemUseCase.execute(new GetInventoryItemQuery(inventoryItemId));
        if (item.plotterPaperRoll()) {
            throw new ProductionDomainException(
                    "Plotter paper must be consumed through Plotter, not as a generic production consumption"
            );
        }
    }

    @Override
    public ProductionMaterialConsumptionInventoryResult consumeMaterial(
            UUID inventoryItemId,
            BigDecimal quantity,
            String unitOfMeasure,
            UUID productionMaterialConsumptionId,
            String observation
    ) {
        ConsumeInventoryMaterialResult result = consumeInventoryMaterialUseCase.execute(
                new ConsumeInventoryMaterialCommand(
                        inventoryItemId,
                        quantity,
                        unitOfMeasure,
                        InventoryMovementSourceType.PRODUCTION,
                        productionMaterialConsumptionId,
                        observation
                )
        );

        return new ProductionMaterialConsumptionInventoryResult(
                result.movementId(),
                result.inventoryItemId(),
                result.materialName(),
                result.materialCode(),
                result.resultingStock(),
                result.unitCost(),
                result.totalCost(),
                result.alreadyProcessed()
        );
    }
}
