package com.magyen.platform.inventory.application.usecase;

import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.InventoryMovement;
import com.magyen.platform.inventory.domain.InventoryMovementRepository;
import com.magyen.platform.inventory.domain.InventoryMovementSourceType;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Devuelve exactamente una vez el papel consumido por un trabajo de Plotter.
 * <p>
 * El OUT original permanece como evidencia. La reversión usa
 * {@code sourceType = PLOTTER_REVERSAL} y {@code sourceId = plotterJobId}.
 * Un reintento encuentra esa reversión y no vuelve a aumentar el stock.
 */
public class ReversePlotterPaperConsumptionUseCase {

    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    public ReversePlotterPaperConsumptionUseCase(
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
    public void execute(UUID plotterJobId) {
        Objects.requireNonNull(plotterJobId, "Plotter job id must not be null");
        if (inventoryMovementRepository
                .findBySourceTypeAndSourceId(InventoryMovementSourceType.PLOTTER_REVERSAL, plotterJobId)
                .isPresent()) {
            return;
        }

        Optional<InventoryMovement> consumption = inventoryMovementRepository
                .findBySourceTypeAndSourceId(InventoryMovementSourceType.PLOTTER, plotterJobId);
        if (consumption.isEmpty()) {
            return;
        }

        InventoryMovement movement = consumption.get();
        InventoryItem roll = inventoryItemRepository.findById(movement.getInventoryItemId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Inventory item not found: " + movement.getInventoryItemId()
                ));
        InventoryMovement reversal = roll.restorePlotterConsumption(
                movement.getQuantity(),
                plotterJobId,
                LocalDateTime.now()
        );
        inventoryItemRepository.saveWithMovement(roll, reversal);
    }

    public boolean hasPaperConsumption(UUID plotterJobId) {
        Objects.requireNonNull(plotterJobId, "Plotter job id must not be null");
        return inventoryMovementRepository
                .findBySourceTypeAndSourceId(InventoryMovementSourceType.PLOTTER, plotterJobId)
                .isPresent();
    }
}
