package com.magyen.platform.inventory.application.usecase;

import com.magyen.platform.inventory.application.dto.DecreaseInventoryStockCommand;
import com.magyen.platform.inventory.application.dto.DecreaseInventoryStockResult;
import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;

import java.util.Objects;

/**
 * Caso de uso que coordina la disminución de stock de un material de inventario.
 */
public class DecreaseInventoryStockUseCase {

    private final InventoryItemRepository inventoryItemRepository;

    public DecreaseInventoryStockUseCase(InventoryItemRepository inventoryItemRepository) {
        this.inventoryItemRepository = Objects.requireNonNull(
                inventoryItemRepository,
                "Inventory item repository must not be null"
        );
    }

    public DecreaseInventoryStockResult execute(DecreaseInventoryStockCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        InventoryItem inventoryItem = inventoryItemRepository.findById(command.inventoryItemId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Inventory item not found: " + command.inventoryItemId()
                ));

        inventoryItem.decreaseStock(command.quantity());

        InventoryItem savedInventoryItem = inventoryItemRepository.save(inventoryItem);

        return new DecreaseInventoryStockResult(
                savedInventoryItem.getId(),
                savedInventoryItem.getStock(),
                savedInventoryItem.getStatus()
        );
    }

    private void validateCommand(DecreaseInventoryStockCommand command) {
        Objects.requireNonNull(command.inventoryItemId(), "Inventory item id must not be null");
        Objects.requireNonNull(command.quantity(), "Quantity must not be null");
    }
}
