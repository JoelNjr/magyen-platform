package com.magyen.platform.inventory.application.usecase;

import com.magyen.platform.inventory.application.dto.IncreaseInventoryStockCommand;
import com.magyen.platform.inventory.application.dto.IncreaseInventoryStockResult;
import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;

import java.util.Objects;

/**
 * Caso de uso que coordina el incremento de stock de un material de inventario.
 */
public class IncreaseInventoryStockUseCase {

    private final InventoryItemRepository inventoryItemRepository;

    public IncreaseInventoryStockUseCase(InventoryItemRepository inventoryItemRepository) {
        this.inventoryItemRepository = Objects.requireNonNull(
                inventoryItemRepository,
                "Inventory item repository must not be null"
        );
    }

    public IncreaseInventoryStockResult execute(IncreaseInventoryStockCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        InventoryItem inventoryItem = inventoryItemRepository.findById(command.inventoryItemId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Inventory item not found: " + command.inventoryItemId()
                ));

        inventoryItem.increaseStock(command.quantity());

        InventoryItem savedInventoryItem = inventoryItemRepository.save(inventoryItem);

        return new IncreaseInventoryStockResult(
                savedInventoryItem.getId(),
                savedInventoryItem.getStock(),
                savedInventoryItem.getStatus()
        );
    }

    private void validateCommand(IncreaseInventoryStockCommand command) {
        Objects.requireNonNull(command.inventoryItemId(), "Inventory item id must not be null");
        Objects.requireNonNull(command.quantity(), "Quantity must not be null");
    }
}
