package com.magyen.platform.inventory.application.usecase;

import com.magyen.platform.inventory.application.dto.GetInventoryMaterialQuery;
import com.magyen.platform.inventory.application.dto.GetInventoryMaterialResult;
import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.MaterialCode;

import java.util.List;
import java.util.Objects;

/**
 * Consulta un material de catálogo y sus unidades físicas existentes.
 */
public class GetInventoryMaterialUseCase {

    private final InventoryItemRepository inventoryItemRepository;

    public GetInventoryMaterialUseCase(InventoryItemRepository inventoryItemRepository) {
        this.inventoryItemRepository = Objects.requireNonNull(
                inventoryItemRepository,
                "Inventory item repository must not be null"
        );
    }

    public GetInventoryMaterialResult execute(GetInventoryMaterialQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        if (query.materialCode() == null || query.materialCode().isBlank()) {
            throw new IllegalArgumentException("Material code must not be blank");
        }

        List<InventoryItem> units = inventoryItemRepository.findAllByMaterialCode(
                MaterialCode.of(query.materialCode())
        );
        if (units.isEmpty()) {
            throw new IllegalArgumentException("Inventory material not found: " + query.materialCode().trim());
        }

        return new GetInventoryMaterialResult(
                InventoryCatalogAssembler.assembleMaterial(units),
                InventoryCatalogAssembler.toOrderedUnitResults(units)
        );
    }
}
