package com.magyen.platform.intelligence.application.usecase;

import com.magyen.platform.intelligence.application.dto.GetInventoryReportResult;
import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.InventoryMaterialType;
import com.magyen.platform.inventory.domain.InventoryMovement;
import com.magyen.platform.inventory.domain.MaterialCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GetInventoryReportUseCaseTest {

    @Test
    void activeItemBelowMinimumAppears() {
        InventoryItem belowMinimum = fabric("MAT-LOW", "2.0000", "5.0000");

        GetInventoryReportResult result = reportOf(belowMinimum);

        assertEquals(1, result.items().size());
        assertEquals(belowMinimum.getId(), result.items().getFirst().inventoryItemId());
        assertEquals("MAT-LOW", result.items().getFirst().materialCode());
    }

    @Test
    void activeItemAtOrAboveMinimumDoesNotAppear() {
        InventoryItem atMinimum = fabric("MAT-EQ", "5.0000", "5.0000");
        InventoryItem aboveMinimum = fabric("MAT-OK", "8.0000", "5.0000");

        GetInventoryReportResult result = reportOf(atMinimum, aboveMinimum);

        assertTrue(result.items().isEmpty());
    }

    @Test
    void inactiveItemBelowMinimumDoesNotAppear() {
        InventoryItem inactiveBelow = fabric("MAT-OFF", "1.0000", "5.0000");
        inactiveBelow.deactivate();

        GetInventoryReportResult result = reportOf(inactiveBelow);

        assertTrue(result.items().isEmpty());
    }

    @Test
    void inactiveZeroStockWithPositiveMinimumDoesNotAppear() {
        InventoryItem inactiveZero = fabric("MAT-ZERO", "0.0000", "3.0000");
        inactiveZero.deactivate();

        GetInventoryReportResult result = reportOf(inactiveZero);

        assertTrue(result.items().isEmpty());
    }

    @Test
    void activeLowStockStillAppearsWhenInactiveNeighborsExist() {
        InventoryItem activeLow = fabric("MAT-ON", "1.0000", "4.0000");
        InventoryItem inactiveLow = fabric("MAT-OFF", "0.0000", "4.0000");
        inactiveLow.deactivate();

        GetInventoryReportResult result = reportOf(activeLow, inactiveLow);

        assertEquals(1, result.items().size());
        assertEquals(activeLow.getId(), result.items().getFirst().inventoryItemId());
        assertEquals("MAT-ON", result.items().getFirst().materialCode());
        assertEquals(0, new BigDecimal("1.0000").compareTo(result.items().getFirst().stock()));
        assertEquals(0, new BigDecimal("4.0000").compareTo(result.items().getFirst().minimumStock()));
    }

    private static GetInventoryReportResult reportOf(InventoryItem... items) {
        return new GetInventoryReportUseCase(new InMemoryInventoryItemRepository(items)).execute();
    }

    private static InventoryItem fabric(String materialCode, String stock, String minimumStock) {
        return InventoryItem.create(
                MaterialCode.of(materialCode),
                "Tela reporte",
                "FABRIC",
                "METER",
                new BigDecimal(stock),
                new BigDecimal(minimumStock)
        );
    }

    private static final class InMemoryInventoryItemRepository implements InventoryItemRepository {

        private final Map<UUID, InventoryItem> items = new LinkedHashMap<>();

        private InMemoryInventoryItemRepository(InventoryItem... inventoryItems) {
            for (InventoryItem inventoryItem : inventoryItems) {
                items.put(inventoryItem.getId(), inventoryItem);
            }
        }

        @Override
        public InventoryItem save(InventoryItem inventoryItem) {
            items.put(inventoryItem.getId(), inventoryItem);
            return inventoryItem;
        }

        @Override
        public InventoryItem saveWithMovement(InventoryItem inventoryItem, InventoryMovement inventoryMovement) {
            return save(inventoryItem);
        }

        @Override
        public Optional<InventoryItem> findById(UUID id) {
            return Optional.ofNullable(items.get(id));
        }

        @Override
        public Optional<InventoryItem> findByCode(MaterialCode materialCode) {
            return items.values().stream()
                    .filter(item -> item.getMaterialCode().equals(materialCode))
                    .findFirst();
        }

        @Override
        public List<InventoryItem> findAllByMaterialCode(MaterialCode materialCode) {
            return items.values().stream()
                    .filter(item -> item.getMaterialCode().equals(materialCode))
                    .toList();
        }

        @Override
        public Optional<InventoryItem> findFirstByMaterialType(InventoryMaterialType materialType) {
            return items.values().stream()
                    .filter(item -> item.getMaterialType() == materialType)
                    .findFirst();
        }

        @Override
        public boolean existsNonPaperWithCode(MaterialCode materialCode) {
            return items.values().stream()
                    .anyMatch(item -> item.getMaterialCode().equals(materialCode)
                            && item.getPaperRollNumber() == null);
        }

        @Override
        public List<InventoryItem> findAll() {
            return List.copyOf(items.values());
        }
    }
}
