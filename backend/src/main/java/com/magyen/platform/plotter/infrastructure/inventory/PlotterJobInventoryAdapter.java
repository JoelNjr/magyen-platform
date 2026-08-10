package com.magyen.platform.plotter.infrastructure.inventory;

import com.magyen.platform.inventory.application.dto.ConsumeInventoryMaterialCommand;
import com.magyen.platform.inventory.application.dto.ConsumeInventoryMaterialResult;
import com.magyen.platform.inventory.application.usecase.ConsumeInventoryMaterialUseCase;
import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.InventoryMovementSourceType;
import com.magyen.platform.inventory.domain.InventoryUnitOfMeasure;
import com.magyen.platform.plotter.application.port.PlotterJobInventoryConsumeResult;
import com.magyen.platform.plotter.application.port.PlotterJobInventoryPort;
import com.magyen.platform.plotter.application.port.PlotterPaperRollView;
import com.magyen.platform.plotter.domain.exception.PlotterDomainException;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Adaptador que traduce el puerto de Plotter a capacidades de Inventory.
 */
public class PlotterJobInventoryAdapter implements PlotterJobInventoryPort {

    private final InventoryItemRepository inventoryItemRepository;
    private final ConsumeInventoryMaterialUseCase consumeInventoryMaterialUseCase;

    public PlotterJobInventoryAdapter(
            InventoryItemRepository inventoryItemRepository,
            ConsumeInventoryMaterialUseCase consumeInventoryMaterialUseCase
    ) {
        this.inventoryItemRepository = Objects.requireNonNull(
                inventoryItemRepository,
                "Inventory item repository must not be null"
        );
        this.consumeInventoryMaterialUseCase = Objects.requireNonNull(
                consumeInventoryMaterialUseCase,
                "Consume inventory material use case must not be null"
        );
    }

    @Override
    public PlotterPaperRollView requirePlotterPaperRoll(UUID paperInventoryItemId) {
        Objects.requireNonNull(paperInventoryItemId, "Paper inventory item id must not be null");

        InventoryItem inventoryItem = inventoryItemRepository.findById(paperInventoryItemId)
                .orElseThrow(() -> new PlotterDomainException(
                        "Inventory item not found: " + paperInventoryItemId
                ));

        if (!inventoryItem.isPlotterPaperRoll()) {
            throw new PlotterDomainException(
                    "Selected inventory item is not a valid Plotter paper roll"
            );
        }

        if (!inventoryItem.getUnitOfMeasureValue().isCompatibleWith(InventoryUnitOfMeasure.METER)) {
            throw new PlotterDomainException(
                    "Plotter paper rolls must use METER as unit of measure"
            );
        }

        return new PlotterPaperRollView(
                inventoryItem.getId(),
                inventoryItem.getPaperRollNumber(),
                inventoryItem.getStock(),
                inventoryItem.getUnitOfMeasure()
        );
    }

    @Override
    public PlotterJobInventoryConsumeResult consumePaperMeters(
            UUID paperInventoryItemId,
            BigDecimal printedMeters,
            UUID plotterJobId,
            String observation
    ) {
        ConsumeInventoryMaterialResult result = consumeInventoryMaterialUseCase.execute(
                new ConsumeInventoryMaterialCommand(
                        paperInventoryItemId,
                        printedMeters,
                        InventoryUnitOfMeasure.METER.getCode(),
                        InventoryMovementSourceType.PLOTTER,
                        plotterJobId,
                        observation
                )
        );

        return new PlotterJobInventoryConsumeResult(
                result.movementId(),
                result.inventoryItemId(),
                result.resultingStock(),
                result.unitCost(),
                result.totalCost(),
                result.alreadyProcessed()
        );
    }
}
