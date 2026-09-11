package com.magyen.platform.inventory.application.usecase;

import com.magyen.platform.inventory.application.dto.GetInventoryItemResult;
import com.magyen.platform.inventory.application.dto.InventoryCatalogItemResult;
import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemStatus;
import com.magyen.platform.inventory.domain.InventoryMaterialType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Agrupa unidades físicas por {@code materialCode} sin persistir stock consolidado.
 */
final class InventoryCatalogAssembler {

    private static final Comparator<InventoryItem> UNIT_ORDER = Comparator
            .comparing(InventoryItem::getPaperRollNumber, Comparator.nullsLast(String::compareTo))
            .thenComparing(item -> item.getId().toString());

    private InventoryCatalogAssembler() {
    }

    static List<InventoryCatalogItemResult> assembleCatalog(List<InventoryItem> inventoryItems) {
        Map<String, List<InventoryItem>> unitsByCode = new LinkedHashMap<>();
        for (InventoryItem inventoryItem : inventoryItems) {
            String materialCode = inventoryItem.getMaterialCode().getValue();
            unitsByCode.computeIfAbsent(materialCode, ignored -> new ArrayList<>()).add(inventoryItem);
        }

        return unitsByCode.values().stream()
                .map(InventoryCatalogAssembler::assembleMaterial)
                .sorted(Comparator.comparing(InventoryCatalogItemResult::materialCode))
                .toList();
    }

    static InventoryCatalogItemResult assembleMaterial(List<InventoryItem> units) {
        Objects.requireNonNull(units, "Units must not be null");
        if (units.isEmpty()) {
            throw new IllegalArgumentException("Catalog material requires at least one inventory item");
        }

        List<InventoryItem> orderedUnits = units.stream().sorted(UNIT_ORDER).toList();
        boolean paperMaterial = orderedUnits.stream()
                .anyMatch(unit -> unit.getMaterialType() == InventoryMaterialType.PAPER);
        InventoryItem representative = orderedUnits.stream()
                .filter(unit -> unit.getMaterialType() == InventoryMaterialType.PAPER)
                .findFirst()
                .orElse(orderedUnits.getFirst());

        BigDecimal aggregatedStock = BigDecimal.ZERO;
        boolean anyActive = false;
        boolean anyLowStock = false;
        BigDecimal firstUnitCost = orderedUnits.getFirst().getUnitCost();
        boolean unitCostUniform = true;
        BigDecimal firstMinimumStock = orderedUnits.getFirst().getMinimumStock();
        boolean minimumStockUniform = true;

        for (InventoryItem unit : orderedUnits) {
            aggregatedStock = aggregatedStock.add(unit.getStock());
            if (unit.getStatus() == InventoryItemStatus.ACTIVE) {
                anyActive = true;
            }
            if (unit.isLowStock()) {
                anyLowStock = true;
            }
            if (!sameNullableDecimal(firstUnitCost, unit.getUnitCost())) {
                unitCostUniform = false;
            }
            if (!sameNullableDecimal(firstMinimumStock, unit.getMinimumStock())) {
                minimumStockUniform = false;
            }
        }

        UUID stockHoldingItemId = paperMaterial ? null : representative.getId();

        return new InventoryCatalogItemResult(
                representative.getMaterialCode().getValue(),
                representative.getName(),
                representative.getCategory(),
                representative.getMaterialType(),
                representative.getUnitOfMeasure(),
                representative.getDescription(),
                aggregatedStock,
                minimumStockUniform ? firstMinimumStock : null,
                minimumStockUniform,
                anyLowStock,
                anyActive ? InventoryItemStatus.ACTIVE : InventoryItemStatus.INACTIVE,
                unitCostUniform ? firstUnitCost : null,
                unitCostUniform,
                orderedUnits.size(),
                paperMaterial,
                stockHoldingItemId
        );
    }

    static List<GetInventoryItemResult> toOrderedUnitResults(List<InventoryItem> units) {
        return units.stream()
                .sorted(UNIT_ORDER)
                .map(InventoryItemReadMapper::toResult)
                .toList();
    }

    private static boolean sameNullableDecimal(BigDecimal left, BigDecimal right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        return left.compareTo(right) == 0;
    }
}
