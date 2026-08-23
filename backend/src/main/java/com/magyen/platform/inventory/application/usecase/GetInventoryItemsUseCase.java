package com.magyen.platform.inventory.application.usecase;

import com.magyen.platform.inventory.application.dto.GetInventoryItemsQuery;
import com.magyen.platform.inventory.application.dto.GetInventoryItemsResult;
import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.InventoryMaterialType;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Caso de uso que lista los materiales de inventario.
 */
public class GetInventoryItemsUseCase {

    private final InventoryItemRepository inventoryItemRepository;

    public GetInventoryItemsUseCase(InventoryItemRepository inventoryItemRepository) {
        this.inventoryItemRepository = Objects.requireNonNull(
                inventoryItemRepository,
                "Inventory item repository must not be null"
        );
    }

    public GetInventoryItemsResult execute() {
        return execute(GetInventoryItemsQuery.all());
    }

    public GetInventoryItemsResult execute(GetInventoryItemsQuery query) {
        Objects.requireNonNull(query, "Query must not be null");

        Stream<InventoryItem> stream = inventoryItemRepository.findAll().stream();

        if (query.materialType() != null && !query.materialType().isBlank()) {
            InventoryMaterialType materialType = InventoryMaterialType.of(query.materialType());
            stream = stream.filter(item -> item.getMaterialType() == materialType);
        }

        if (Boolean.TRUE.equals(query.plotterPaperRoll())) {
            stream = stream.filter(InventoryItem::isPlotterPaperRoll)
                    .filter(item -> item.getStock().compareTo(BigDecimal.ZERO) > 0);
        }

        return new GetInventoryItemsResult(
                stream.map(InventoryItemReadMapper::toResult).toList()
        );
    }
}
