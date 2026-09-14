package com.magyen.platform.inventory.application.usecase;

import com.magyen.platform.inventory.application.dto.GetInventoryCatalogResult;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.InventoryItemStatus;

import java.util.Objects;

/**
 * Lista el inventario agrupado por código de material, sin alterar unidades físicas.
 */
public class GetInventoryCatalogUseCase {

    private final InventoryItemRepository inventoryItemRepository;

    public GetInventoryCatalogUseCase(InventoryItemRepository inventoryItemRepository) {
        this.inventoryItemRepository = Objects.requireNonNull(
                inventoryItemRepository,
                "Inventory item repository must not be null"
        );
    }

    public GetInventoryCatalogResult execute() {
        return new GetInventoryCatalogResult(
                InventoryCatalogAssembler.assembleCatalog(inventoryItemRepository.findAll()).stream()
                        .filter(material -> material.status() == InventoryItemStatus.ACTIVE)
                        .toList()
        );
    }
}
