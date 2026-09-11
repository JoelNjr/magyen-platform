package com.magyen.platform.inventory.application.usecase;

import com.magyen.platform.inventory.application.dto.GetInventoryCatalogResult;
import com.magyen.platform.inventory.application.dto.GetInventoryItemResult;
import com.magyen.platform.inventory.application.dto.GetInventoryMaterialQuery;
import com.magyen.platform.inventory.application.dto.GetInventoryMaterialResult;
import com.magyen.platform.inventory.application.dto.InventoryCatalogItemResult;
import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.InventoryItemStatus;
import com.magyen.platform.inventory.domain.InventoryMaterialType;
import com.magyen.platform.inventory.domain.InventoryMovement;
import com.magyen.platform.inventory.domain.MaterialCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GetInventoryCatalogUseCaseTest {

    @Test
    void emptyInventoryReturnsEmptyCatalog() {
        GetInventoryCatalogUseCase useCase = new GetInventoryCatalogUseCase(new InMemoryInventoryItemRepository());

        GetInventoryCatalogResult result = useCase.execute();

        assertTrue(result.materials().isEmpty());
    }

    @Test
    void twoPaperUnitsWithSameMaterialCodeProduceOneCatalogRow() {
        String materialCode = "MAT-001";
        InMemoryInventoryItemRepository repository = new InMemoryInventoryItemRepository(
                paper(materialCode, "RP-001", "20.0000", "100.00"),
                paper(materialCode, "RP-002", "33.1000", "100.00")
        );

        InventoryCatalogItemResult catalog = catalogOf(repository, materialCode);

        assertEquals(materialCode, catalog.materialCode());
        assertEquals(InventoryMaterialType.PAPER, catalog.materialType());
        assertEquals(0, new BigDecimal("53.1000").compareTo(catalog.aggregatedStock()));
        assertEquals(2, catalog.physicalUnitCount());
        assertTrue(catalog.paperMaterial());
        assertNull(catalog.stockHoldingItemId());
        assertTrue(catalog.unitCostUniform());
        assertEquals(0, new BigDecimal("100.00").compareTo(catalog.unitCost()));
    }

    @Test
    void threePaperUnitsWithSameMaterialCodeRemainOneCatalogRow() {
        String materialCode = "MAT-001";
        InMemoryInventoryItemRepository repository = new InMemoryInventoryItemRepository(
                paper(materialCode, "RP-001", "10.0000", "80.00"),
                paper(materialCode, "RP-002", "15.0000", "90.00"),
                paper(materialCode, "RP-003", "5.0000", "100.00")
        );

        InventoryCatalogItemResult catalog = catalogOf(repository, materialCode);

        assertEquals(1, countCatalogRows(repository, materialCode));
        assertEquals(3, catalog.physicalUnitCount());
        assertEquals(0, new BigDecimal("30.0000").compareTo(catalog.aggregatedStock()));
        assertFalse(catalog.unitCostUniform());
        assertNull(catalog.unitCost());
    }

    @Test
    void paperUnitsRemainIndependentAndKeepTheirRollNumbers() {
        InventoryItem first = paper("MAT-001", "RP-001", "20.0000", "100.00");
        InventoryItem second = paper("MAT-001", "RP-002", "33.1000", "120.00");
        InMemoryInventoryItemRepository repository = new InMemoryInventoryItemRepository(first, second);

        GetInventoryMaterialResult detail = new GetInventoryMaterialUseCase(repository)
                .execute(new GetInventoryMaterialQuery("MAT-001"));

        assertEquals(2, detail.units().size());
        assertEquals(first.getId(), detail.units().get(0).inventoryItemId());
        assertEquals("RP-001", detail.units().get(0).paperRollNumber());
        assertEquals(0, first.getStock().compareTo(detail.units().get(0).stock()));
        assertEquals(second.getId(), detail.units().get(1).inventoryItemId());
        assertEquals("RP-002", detail.units().get(1).paperRollNumber());
        assertEquals(0, second.getStock().compareTo(detail.units().get(1).stock()));
        assertTrue(detail.units().get(0).plotterPaperRoll());
        assertTrue(detail.units().get(1).plotterPaperRoll());
    }

    @Test
    void differentMaterialCodesAreNotGrouped() {
        InMemoryInventoryItemRepository repository = new InMemoryInventoryItemRepository(
                paper("MAT-001", "RP-001", "10.0000", "100.00"),
                paper("MAT-009", "RP-010", "8.0000", "100.00"),
                ink("MAT-004", "8.0000")
        );

        GetInventoryCatalogResult catalog = new GetInventoryCatalogUseCase(repository).execute();

        assertEquals(3, catalog.materials().size());
        assertEquals(List.of("MAT-001", "MAT-004", "MAT-009"),
                catalog.materials().stream().map(InventoryCatalogItemResult::materialCode).toList());
    }

    @Test
    void paperWithoutRollNumberIsNotConvertedIntoAPlotterRoll() {
        InventoryItem paperWithoutRoll = InventoryItem.create(
                MaterialCode.of("MAT-088"),
                "Papel legado",
                "PAPER",
                "METER",
                new BigDecimal("12.0000"),
                null,
                "Sin RP",
                new BigDecimal("50.00"),
                InventoryMaterialType.PAPER,
                null
        );
        InMemoryInventoryItemRepository repository = new InMemoryInventoryItemRepository(paperWithoutRoll);

        GetInventoryMaterialResult detail = new GetInventoryMaterialUseCase(repository)
                .execute(new GetInventoryMaterialQuery("MAT-088"));

        assertTrue(detail.material().paperMaterial());
        assertEquals(1, detail.units().size());
        assertNull(detail.units().getFirst().paperRollNumber());
        assertFalse(detail.units().getFirst().plotterPaperRoll());
    }

    @Test
    void singleRollCatalogWorks() {
        InMemoryInventoryItemRepository repository = new InMemoryInventoryItemRepository(
                paper("MAT-001", "RP-001", "40.0000", "90.00")
        );

        InventoryCatalogItemResult catalog = catalogOf(repository, "MAT-001");

        assertEquals(1, catalog.physicalUnitCount());
        assertEquals(0, new BigDecimal("40.0000").compareTo(catalog.aggregatedStock()));
        assertTrue(catalog.paperMaterial());
    }

    @Test
    void manyRollsStayInOneCatalogRow() {
        List<InventoryItem> rolls = new ArrayList<>();
        for (int index = 1; index <= 12; index++) {
            rolls.add(paper("MAT-001", "RP-" + String.format("%03d", index), "1.0000", "10.00"));
        }
        InMemoryInventoryItemRepository repository = new InMemoryInventoryItemRepository(rolls);

        InventoryCatalogItemResult catalog = catalogOf(repository, "MAT-001");
        GetInventoryMaterialResult detail = new GetInventoryMaterialUseCase(repository)
                .execute(new GetInventoryMaterialQuery("MAT-001"));

        assertEquals(1, countCatalogRows(repository, "MAT-001"));
        assertEquals(12, catalog.physicalUnitCount());
        assertEquals(0, new BigDecimal("12.0000").compareTo(catalog.aggregatedStock()));
        assertEquals(12, detail.units().size());
        assertEquals("RP-001", detail.units().getFirst().paperRollNumber());
        assertEquals("RP-012", detail.units().getLast().paperRollNumber());
    }

    @Test
    void inkKeepsItsOwnStockHoldingItem() {
        InventoryItem ink = ink("MAT-004", "8.0000");
        InMemoryInventoryItemRepository repository = new InMemoryInventoryItemRepository(ink);

        InventoryCatalogItemResult catalog = catalogOf(repository, "MAT-004");

        assertFalse(catalog.paperMaterial());
        assertEquals(ink.getId(), catalog.stockHoldingItemId());
        assertEquals(1, catalog.physicalUnitCount());
        assertEquals(0, new BigDecimal("8.0000").compareTo(catalog.aggregatedStock()));
    }

    @Test
    void catalogReadDoesNotMutateExistingUnits() {
        InventoryItem first = paper("MAT-001", "RP-001", "20.0000", "100.00");
        InventoryItem second = paper("MAT-001", "RP-002", "33.1000", "100.00");
        InMemoryInventoryItemRepository repository = new InMemoryInventoryItemRepository(first, second);

        new GetInventoryCatalogUseCase(repository).execute();
        new GetInventoryMaterialUseCase(repository).execute(new GetInventoryMaterialQuery("MAT-001"));

        InventoryItem reloadedFirst = repository.findById(first.getId()).orElseThrow();
        InventoryItem reloadedSecond = repository.findById(second.getId()).orElseThrow();
        assertEquals(first.getId(), reloadedFirst.getId());
        assertEquals(second.getId(), reloadedSecond.getId());
        assertEquals("RP-001", reloadedFirst.getPaperRollNumber());
        assertEquals("RP-002", reloadedSecond.getPaperRollNumber());
        assertEquals(0, first.getStock().compareTo(reloadedFirst.getStock()));
        assertEquals(0, second.getStock().compareTo(reloadedSecond.getStock()));
        assertEquals(InventoryItemStatus.ACTIVE, reloadedFirst.getStatus());
    }

    @Test
    void unknownMaterialCodeIsRejected() {
        GetInventoryMaterialUseCase useCase = new GetInventoryMaterialUseCase(new InMemoryInventoryItemRepository());

        assertThrows(IllegalArgumentException.class, () ->
                useCase.execute(new GetInventoryMaterialQuery("MAT-MISSING"))
        );
        assertThrows(IllegalArgumentException.class, () ->
                useCase.execute(new GetInventoryMaterialQuery("  "))
        );
    }

    @Test
    void unitResultsExposeOriginalIdentifiersForOperationalClients() {
        InventoryItem roll = paper("MAT-001", "RP-001", "20.0000", "100.00");
        GetInventoryItemResult unit = InventoryCatalogAssembler.toOrderedUnitResults(List.of(roll)).getFirst();

        assertEquals(roll.getId(), unit.inventoryItemId());
        assertEquals("MAT-001", unit.materialCode());
        assertEquals("RP-001", unit.paperRollNumber());
        assertTrue(unit.plotterPaperRoll());
    }

    private static InventoryCatalogItemResult catalogOf(
            InMemoryInventoryItemRepository repository,
            String materialCode
    ) {
        return new GetInventoryCatalogUseCase(repository).execute().materials().stream()
                .filter(item -> materialCode.equals(item.materialCode()))
                .findFirst()
                .orElseThrow();
    }

    private static long countCatalogRows(InMemoryInventoryItemRepository repository, String materialCode) {
        return new GetInventoryCatalogUseCase(repository).execute().materials().stream()
                .filter(item -> materialCode.equals(item.materialCode()))
                .count();
    }

    private static InventoryItem paper(String materialCode, String rollNumber, String stock, String unitCost) {
        return InventoryItem.create(
                MaterialCode.of(materialCode),
                "Papel Plotter",
                "PAPER",
                "METER",
                new BigDecimal(stock),
                new BigDecimal("5.0000"),
                "Papel sublimación",
                new BigDecimal(unitCost),
                InventoryMaterialType.PAPER,
                rollNumber
        );
    }

    private static InventoryItem ink(String materialCode, String stock) {
        return InventoryItem.create(
                MaterialCode.of(materialCode),
                "Tinta cian",
                "INK",
                "LITER",
                new BigDecimal(stock),
                new BigDecimal("1.0000"),
                "Tinta",
                new BigDecimal("25000.00"),
                InventoryMaterialType.INK,
                null
        );
    }

    private static final class InMemoryInventoryItemRepository implements InventoryItemRepository {

        private final Map<UUID, InventoryItem> items = new LinkedHashMap<>();

        private InMemoryInventoryItemRepository(InventoryItem... inventoryItems) {
            this(List.of(inventoryItems));
        }

        private InMemoryInventoryItemRepository(List<InventoryItem> inventoryItems) {
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
