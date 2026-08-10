package com.magyen.platform.intelligence.application.usecase;

import com.magyen.platform.intelligence.application.dto.GetInventoryReportResult;
import com.magyen.platform.intelligence.application.dto.GetInventoryReportResult.LowStockItem;
import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;

import java.util.List;
import java.util.Objects;

/**
 * Caso de uso que consolida el reporte de inventario con materiales bajo el stock mínimo.
 * <p>
 * Solo consulta información existente; no modifica el estado del negocio.
 */
public class GetInventoryReportUseCase {

    private final InventoryItemRepository inventoryItemRepository;

    public GetInventoryReportUseCase(InventoryItemRepository inventoryItemRepository) {
        this.inventoryItemRepository = Objects.requireNonNull(
                inventoryItemRepository,
                "Inventory item repository must not be null"
        );
    }

    public GetInventoryReportResult execute() {
        List<InventoryItem> inventoryItems = inventoryItemRepository.findAll();

        List<LowStockItem> lowStockItems = inventoryItems.stream()
                .filter(item -> item.getMinimumStock() != null
                        && item.getStock().compareTo(item.getMinimumStock()) < 0)
                .map(this::toLowStockItem)
                .toList();

        return new GetInventoryReportResult(lowStockItems);
    }

    private LowStockItem toLowStockItem(InventoryItem inventoryItem) {
        return new LowStockItem(
                inventoryItem.getId(),
                inventoryItem.getMaterialCode().getValue(),
                inventoryItem.getName(),
                inventoryItem.getCategory(),
                inventoryItem.getUnitOfMeasure(),
                inventoryItem.getStock(),
                inventoryItem.getMinimumStock()
        );
    }
}
