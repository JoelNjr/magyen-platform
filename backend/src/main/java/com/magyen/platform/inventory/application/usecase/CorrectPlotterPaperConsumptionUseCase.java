package com.magyen.platform.inventory.application.usecase;

import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.InventoryMovement;
import com.magyen.platform.inventory.domain.InventoryMovementRepository;
import com.magyen.platform.inventory.domain.InventoryMovementSourceType;
import com.magyen.platform.inventory.domain.exception.InventoryDomainException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Corrige el único OUT de un trabajo de Plotter.
 * <p>
 * El índice único (source_type, source_id) impide un segundo consumo con el mismo trabajo.
 * La corrección devuelve o descuenta el delta y reescribe ese movimiento. Una repetición
 * con los mismos metros y el mismo rollo no vuelve a mover stock.
 */
public class CorrectPlotterPaperConsumptionUseCase {

    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    public CorrectPlotterPaperConsumptionUseCase(
            InventoryItemRepository inventoryItemRepository,
            InventoryMovementRepository inventoryMovementRepository
    ) {
        this.inventoryItemRepository = Objects.requireNonNull(
                inventoryItemRepository,
                "Inventory item repository must not be null"
        );
        this.inventoryMovementRepository = Objects.requireNonNull(
                inventoryMovementRepository,
                "Inventory movement repository must not be null"
        );
    }

    @Transactional
    public void execute(
            UUID paperInventoryItemId,
            BigDecimal printedMeters,
            UUID plotterJobId,
            String observation
    ) {
        Objects.requireNonNull(paperInventoryItemId, "Paper inventory item id must not be null");
        Objects.requireNonNull(printedMeters, "Printed meters must not be null");
        Objects.requireNonNull(plotterJobId, "Plotter job id must not be null");
        if (printedMeters.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InventoryDomainException("Printed meters must be greater than zero");
        }

        InventoryMovement movement = inventoryMovementRepository
                .findBySourceTypeAndSourceId(InventoryMovementSourceType.PLOTTER, plotterJobId)
                .orElseThrow(() -> new InventoryDomainException(
                        "Plotter paper consumption was not found for job: " + plotterJobId
                ));

        boolean sameRoll = paperInventoryItemId.equals(movement.getInventoryItemId());
        boolean sameMeters = printedMeters.compareTo(movement.getQuantity()) == 0;
        if (sameRoll && sameMeters) {
            return;
        }

        if (sameRoll) {
            InventoryItem roll = load(paperInventoryItemId);
            roll.applyPlotterConsumptionDelta(printedMeters.subtract(movement.getQuantity()));
            inventoryItemRepository.save(roll);
            inventoryMovementRepository.save(movement.rewritePlotterConsumption(
                    roll.getId(),
                    printedMeters,
                    roll.getStock(),
                    movement.getUnitCost(),
                    observation
            ));
            return;
        }

        InventoryItem previousRoll = load(movement.getInventoryItemId());
        InventoryItem nextRoll = load(paperInventoryItemId);
        previousRoll.applyPlotterConsumptionDelta(movement.getQuantity().negate());
        nextRoll.applyPlotterConsumptionDelta(printedMeters);
        inventoryItemRepository.save(previousRoll);
        inventoryItemRepository.save(nextRoll);
        inventoryMovementRepository.save(movement.rewritePlotterConsumption(
                nextRoll.getId(),
                printedMeters,
                nextRoll.getStock(),
                nextRoll.getUnitCost(),
                observation
        ));
    }

    private InventoryItem load(UUID inventoryItemId) {
        return inventoryItemRepository.findById(inventoryItemId)
                .orElseThrow(() -> new InventoryDomainException(
                        "Inventory item not found: " + inventoryItemId
                ));
    }
}
