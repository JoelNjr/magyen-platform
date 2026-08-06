package com.magyen.platform.inventory.application.usecase;

import com.magyen.platform.inventory.application.dto.GetInventoryItemQuery;
import com.magyen.platform.inventory.application.dto.GetInventoryItemResult;
import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;

import java.util.Objects;

/**
 * Caso de uso que coordina la consulta de un material de inventario existente.
 */
public class GetInventoryItemUseCase {

    private final InventoryItemRepository inventoryItemRepository;

    public GetInventoryItemUseCase(InventoryItemRepository inventoryItemRepository) {
        this.inventoryItemRepository = Objects.requireNonNull(
                inventoryItemRepository,
                "Inventory item repository must not be null"
        );
    }

    public GetInventoryItemResult execute(GetInventoryItemQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        Objects.requireNonNull(query.inventoryItemId(), "Inventory item id must not be null");

        InventoryItem inventoryItem = inventoryItemRepository.findById(query.inventoryItemId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Inventory item not found: " + query.inventoryItemId()
                ));

        return new GetInventoryItemResult(
                inventoryItem.getId(),
                inventoryItem.getMaterialCode().getValue(),
                inventoryItem.getName(),
                inventoryItem.getCategory(),
                inventoryItem.getUnitOfMeasure(),
                inventoryItem.getStock(),
                inventoryItem.getMinimumStock(),
                inventoryItem.getStatus()
        );
    }
}
