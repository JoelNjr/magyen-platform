package com.magyen.platform.inventory.application.usecase;

import com.magyen.platform.inventory.application.dto.ConsumeInventoryMaterialCommand;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemCommand;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemResult;
import com.magyen.platform.inventory.application.dto.GetInventoryItemQuery;
import com.magyen.platform.inventory.application.dto.GetInventoryItemsQuery;
import com.magyen.platform.inventory.application.dto.GetInventoryMaterialQuery;
import com.magyen.platform.inventory.application.dto.GetInventoryMaterialResult;
import com.magyen.platform.inventory.application.dto.GetInventoryMovementsQuery;
import com.magyen.platform.inventory.application.dto.InventoryAcquisitionCommand;
import com.magyen.platform.inventory.application.dto.InventoryCatalogItemResult;
import com.magyen.platform.inventory.application.dto.RegisterInventoryMovementCommand;
import com.magyen.platform.inventory.application.dto.RegisterInventoryPurchaseCommand;
import com.magyen.platform.inventory.application.dto.RegisterInventoryPurchaseResult;
import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.InventoryMaterialType;
import com.magyen.platform.inventory.domain.InventoryMovementSourceType;
import com.magyen.platform.inventory.domain.InventoryMovementType;
import com.magyen.platform.inventory.domain.MaterialCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class InventoryCatalogCompatibilityUseCaseTest {

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private GetInventoryCatalogUseCase getInventoryCatalogUseCase;

    @Autowired
    private GetInventoryMaterialUseCase getInventoryMaterialUseCase;

    @Autowired
    private GetInventoryItemsUseCase getInventoryItemsUseCase;

    @Autowired
    private GetInventoryItemUseCase getInventoryItemUseCase;

    @Autowired
    private GetInventoryMovementsUseCase getInventoryMovementsUseCase;

    @Autowired
    private RegisterInventoryMovementUseCase registerInventoryMovementUseCase;

    @Autowired
    private RegisterInventoryPurchaseUseCase registerInventoryPurchaseUseCase;

    @Autowired
    private CreateInventoryItemUseCase createInventoryItemUseCase;

    @Autowired
    private ConsumeInventoryMaterialUseCase consumeInventoryMaterialUseCase;

    @Test
    void readsPreexistingPaperUnitsAndMovementsWithoutMigration() {
        String materialCode = "PRE-" + suffix();
        InventoryItem first = inventoryItemRepository.save(paper(materialCode, uniqueRoll(), "20.0000", "100.00"));
        InventoryItem second = inventoryItemRepository.save(paper(materialCode, uniqueRoll(), "33.1000", "100.00"));
        registerInventoryMovementUseCase.execute(new RegisterInventoryMovementCommand(
                first.getId(),
                InventoryMovementType.IN,
                new BigDecimal("1.0000"),
                null,
                "historical restock"
        ));

        UUID firstId = first.getId();
        UUID secondId = second.getId();
        String firstRoll = first.getPaperRollNumber();
        String secondRoll = second.getPaperRollNumber();
        BigDecimal firstStockAfterMovement = inventoryItemRepository.findById(firstId).orElseThrow().getStock();

        InventoryCatalogItemResult catalog = catalogOf(materialCode);
        GetInventoryMaterialResult detail = getInventoryMaterialUseCase.execute(
                new GetInventoryMaterialQuery(materialCode)
        );

        assertEquals(1, countCatalogRows(materialCode));
        assertEquals(2, catalog.physicalUnitCount());
        assertEquals(0, firstStockAfterMovement.add(second.getStock()).compareTo(catalog.aggregatedStock()));
        assertTrue(detail.units().stream().anyMatch(unit -> unit.inventoryItemId().equals(firstId)
                && firstRoll.equals(unit.paperRollNumber())));
        assertTrue(detail.units().stream().anyMatch(unit -> unit.inventoryItemId().equals(secondId)
                && secondRoll.equals(unit.paperRollNumber())));

        var movements = getInventoryMovementsUseCase.execute(new GetInventoryMovementsQuery(firstId));
        assertEquals(1, movements.movements().size());
        assertEquals(firstId, movements.movements().getFirst().inventoryItemId());
        assertEquals("historical restock", movements.movements().getFirst().observation());

        InventoryItem unchangedFirst = inventoryItemRepository.findById(firstId).orElseThrow();
        InventoryItem unchangedSecond = inventoryItemRepository.findById(secondId).orElseThrow();
        assertEquals(firstId, unchangedFirst.getId());
        assertEquals(secondId, unchangedSecond.getId());
        assertEquals(materialCode, unchangedFirst.getMaterialCode().getValue());
        assertEquals(firstRoll, unchangedFirst.getPaperRollNumber());
        assertEquals(secondRoll, unchangedSecond.getPaperRollNumber());
        assertEquals(0, firstStockAfterMovement.compareTo(unchangedFirst.getStock()));
        assertEquals(0, second.getStock().compareTo(unchangedSecond.getStock()));
    }

    @Test
    void plotterPaperRollFilterContinuesReturningPhysicalRolls() {
        String materialCode = "PLT-" + suffix();
        InventoryItem available = inventoryItemRepository.save(paper(materialCode, uniqueRoll(), "12.0000", "80.00"));
        InventoryItem exhausted = inventoryItemRepository.save(paper(materialCode, uniqueRoll(), "0.0000", "80.00"));
        inventoryItemRepository.save(ink("INK-" + suffix(), "3.0000"));

        var rolls = getInventoryItemsUseCase.execute(new GetInventoryItemsQuery(null, true));

        assertTrue(rolls.items().stream().anyMatch(item -> item.inventoryItemId().equals(available.getId())
                && item.plotterPaperRoll()
                && available.getPaperRollNumber().equals(item.paperRollNumber())));
        assertFalse(rolls.items().stream().anyMatch(item -> item.inventoryItemId().equals(exhausted.getId())));
        assertTrue(rolls.items().stream().allMatch(item -> item.plotterPaperRoll() && item.inventoryItemId() != null));
    }

    @Test
    void plotterConsumptionUsesRollUuidAndLeavesTheSiblingRollUntouched() {
        String materialCode = "CNS-" + suffix();
        InventoryItem consumed = inventoryItemRepository.save(paper(materialCode, uniqueRoll(), "20.0000", "100.00"));
        InventoryItem sibling = inventoryItemRepository.save(paper(materialCode, uniqueRoll(), "33.1000", "100.00"));

        consumeInventoryMaterialUseCase.execute(new ConsumeInventoryMaterialCommand(
                consumed.getId(),
                new BigDecimal("5.0000"),
                "METER",
                InventoryMovementSourceType.PLOTTER,
                UUID.randomUUID(),
                "plotter consume"
        ));

        assertEquals(0, new BigDecimal("15.0000").compareTo(
                inventoryItemRepository.findById(consumed.getId()).orElseThrow().getStock()));
        assertEquals(0, sibling.getStock().compareTo(
                inventoryItemRepository.findById(sibling.getId()).orElseThrow().getStock()));

        var movements = getInventoryMovementsUseCase.execute(new GetInventoryMovementsQuery(consumed.getId()));
        assertEquals(1, movements.movements().size());
        assertEquals(consumed.getId(), movements.movements().getFirst().inventoryItemId());
        assertEquals(InventoryMovementSourceType.PLOTTER, movements.movements().getFirst().sourceType());

        InventoryCatalogItemResult catalog = catalogOf(materialCode);
        assertEquals(0, new BigDecimal("48.1000").compareTo(catalog.aggregatedStock()));
    }

    @Test
    void newPaperPurchaseCreatesANewRollAndReusesMaterialCode() {
        CreateInventoryItemResult first = createPaperRoll();
        CreateInventoryItemResult second = createPaperRoll();

        assertEquals(first.materialCode(), second.materialCode());
        assertNotEquals(first.inventoryItemId(), second.inventoryItemId());
        assertNotEquals(first.paperRollNumber(), second.paperRollNumber());
        assertTrue(first.paperRollNumber().matches("RP-\\d{3,}"));
        assertTrue(second.paperRollNumber().matches("RP-\\d{3,}"));

        GetInventoryMaterialResult detail = getInventoryMaterialUseCase.execute(
                new GetInventoryMaterialQuery(first.materialCode())
        );
        assertTrue(detail.units().stream().anyMatch(unit -> unit.inventoryItemId().equals(first.inventoryItemId())));
        assertTrue(detail.units().stream().anyMatch(unit -> unit.inventoryItemId().equals(second.inventoryItemId())));
        assertTrue(detail.material().paperMaterial());
        assertTrue(detail.material().physicalUnitCount() >= 2);
    }

    @Test
    void inkRestockOnExistingItemDoesNotCreateANewMaterialCode() {
        CreateInventoryItemResult ink = createInventoryItemUseCase.execute(new CreateInventoryItemCommand(
                "IGN-" + suffix(),
                "Tinta cian",
                "INK",
                "LITER",
                BigDecimal.ZERO,
                null,
                "Tinta",
                null,
                "INK",
                false,
                new InventoryAcquisitionCommand(
                        UUID.randomUUID(),
                        new BigDecimal("2.0000"),
                        null,
                        new BigDecimal("50000.00"),
                        LocalDate.now(),
                        "initial ink"
                )
        ));

        RegisterInventoryPurchaseResult restock = registerInventoryPurchaseUseCase.execute(
                new RegisterInventoryPurchaseCommand(
                        ink.inventoryItemId(),
                        UUID.randomUUID(),
                        new BigDecimal("3.0000"),
                        new BigDecimal("20000.00"),
                        LocalDate.now(),
                        "ink restock"
                )
        );

        assertEquals(ink.materialCode(), restock.materialCode());
        assertEquals(ink.inventoryItemId(), restock.inventoryItemId());
        assertEquals(0, new BigDecimal("5.0000").compareTo(restock.resultingStock()));
        assertEquals(1, countCatalogRows(ink.materialCode()));
        assertEquals(ink.inventoryItemId(), getInventoryItemUseCase.execute(
                new GetInventoryItemQuery(ink.inventoryItemId())
        ).inventoryItemId());
    }

    private InventoryCatalogItemResult catalogOf(String materialCode) {
        return getInventoryCatalogUseCase.execute().materials().stream()
                .filter(item -> materialCode.equals(item.materialCode()))
                .findFirst()
                .orElseThrow();
    }

    private long countCatalogRows(String materialCode) {
        return getInventoryCatalogUseCase.execute().materials().stream()
                .filter(item -> materialCode.equals(item.materialCode()))
                .count();
    }

    private CreateInventoryItemResult createPaperRoll() {
        return createInventoryItemUseCase.execute(new CreateInventoryItemCommand(
                "IGN-" + suffix(),
                "Papel Plotter",
                "PAPER",
                "METER",
                new BigDecimal("50.0000"),
                null,
                "Rollo",
                null,
                "PAPER",
                true,
                new InventoryAcquisitionCommand(
                        UUID.randomUUID(),
                        BigDecimal.ONE,
                        new BigDecimal("180000.00"),
                        null,
                        LocalDate.now(),
                        "new paper roll"
                )
        ));
    }

    private static InventoryItem paper(String materialCode, String rollNumber, String stock, String unitCost) {
        return InventoryItem.create(
                MaterialCode.of(materialCode),
                "Papel Plotter",
                "PAPER",
                "METER",
                new BigDecimal(stock),
                new BigDecimal("5.0000"),
                "Papel existente",
                new BigDecimal(unitCost),
                InventoryMaterialType.PAPER,
                rollNumber
        );
    }

    private static InventoryItem ink(String materialCode, String stock) {
        return InventoryItem.create(
                MaterialCode.of(materialCode),
                "Tinta",
                "INK",
                "LITER",
                new BigDecimal(stock),
                null,
                "Tinta existente",
                new BigDecimal("15000.00"),
                InventoryMaterialType.INK,
                null
        );
    }

    private static String uniqueRoll() {
        return "RP-C" + suffix();
    }

    private static String suffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
