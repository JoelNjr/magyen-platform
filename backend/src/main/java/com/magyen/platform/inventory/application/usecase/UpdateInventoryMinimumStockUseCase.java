package com.magyen.platform.inventory.application.usecase;

import com.magyen.platform.inventory.application.dto.GetInventoryItemResult;
import com.magyen.platform.inventory.application.dto.UpdateInventoryMinimumStockCommand;
import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;

import java.util.Objects;

/**
 * Caso de uso que configura el umbral mínimo de stock sin alterar existencias ni movimientos.
 */
public class UpdateInventoryMinimumStockUseCase {

    private final InventoryItemRepository inventoryItemRepository;

    public UpdateInventoryMinimumStockUseCase(InventoryItemRepository inventoryItemRepository) {
        this.inventoryItemRepository = Objects.requireNonNull(
                inventoryItemRepository,
                "Inventory item repository must not be null"
        );
    }

    public GetInventoryItemResult execute(UpdateInventoryMinimumStockCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.inventoryItemId(), "Inventory item id must not be null");

        InventoryItem inventoryItem = inventoryItemRepository.findById(command.inventoryItemId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Inventory item not found: " + command.inventoryItemId()
                ));

        inventoryItem.updateMinimumStock(command.minimumStock());
        InventoryItem savedInventoryItem = inventoryItemRepository.save(inventoryItem);

        return InventoryItemReadMapper.toResult(savedInventoryItem);
    }
}
