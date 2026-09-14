package com.magyen.platform.inventory.application.usecase;

import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.inventory.application.dto.ConsumeInventoryMaterialCommand;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemCommand;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemResult;
import com.magyen.platform.inventory.application.dto.DeactivateInventoryMaterialCommand;
import com.magyen.platform.inventory.application.dto.DeactivateInventoryMaterialResult;
import com.magyen.platform.inventory.application.dto.GetInventoryCatalogResult;
import com.magyen.platform.inventory.application.dto.GetInventoryItemQuery;
import com.magyen.platform.inventory.application.dto.GetInventoryItemResult;
import com.magyen.platform.inventory.application.dto.GetInventoryItemsQuery;
import com.magyen.platform.inventory.application.dto.GetInventoryMaterialQuery;
import com.magyen.platform.inventory.application.dto.GetInventoryMaterialResult;
import com.magyen.platform.inventory.application.dto.GetInventoryMovementsQuery;
import com.magyen.platform.inventory.application.dto.IncreaseInventoryStockCommand;
import com.magyen.platform.inventory.application.dto.RegisterInventoryPurchaseCommand;
import com.magyen.platform.inventory.application.dto.RegisterInventoryPurchaseResult;
import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.InventoryItemStatus;
import com.magyen.platform.inventory.domain.InventoryMaterialType;
import com.magyen.platform.inventory.domain.InventoryMovementRepository;
import com.magyen.platform.inventory.domain.InventoryMovementSourceType;
import com.magyen.platform.inventory.domain.MaterialCode;
import com.magyen.platform.inventory.domain.exception.InventoryDomainException;
import com.magyen.platform.plotter.application.dto.CreatePlotterJobCommand;
import com.magyen.platform.plotter.application.dto.CreatePlotterJobResult;
import com.magyen.platform.plotter.application.usecase.CreatePlotterJobUseCase;
import com.magyen.platform.plotter.domain.PlotterJob;
import com.magyen.platform.plotter.domain.PlotterJobRepository;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class DeactivateInventoryMaterialUseCaseTest {

    @Autowired
    private CreateInventoryItemUseCase createInventoryItemUseCase;

    @Autowired
    private RegisterInventoryPurchaseUseCase registerInventoryPurchaseUseCase;

    @Autowired
    private ConsumeInventoryMaterialUseCase consumeInventoryMaterialUseCase;

    @Autowired
    private IncreaseInventoryStockUseCase increaseInventoryStockUseCase;

    @Autowired
    private DeactivateInventoryMaterialUseCase deactivateInventoryMaterialUseCase;

    @Autowired
    private GetInventoryCatalogUseCase getInventoryCatalogUseCase;

    @Autowired
    private GetInventoryItemsUseCase getInventoryItemsUseCase;

    @Autowired
    private GetInventoryMaterialUseCase getInventoryMaterialUseCase;

    @Autowired
    private GetInventoryItemUseCase getInventoryItemUseCase;

    @Autowired
    private GetInventoryMovementsUseCase getInventoryMovementsUseCase;

    @Autowired
    private CreatePlotterJobUseCase createPlotterJobUseCase;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    @Autowired
    private PlotterJobRepository plotterJobRepository;

    @Test
    void activeMaterialAppearsInCatalogAndCanBeDeactivatedWhenStockIsZero() {
        CreateInventoryItemResult created = createInk(BigDecimal.ZERO);
        assertTrue(catalogContains(created.materialCode()));
        assertTrue(itemListContains(created.inventoryItemId()));

        DeactivateInventoryMaterialResult result = deactivate(created.materialCode());

        assertEquals(created.materialCode(), result.materialCode());
        assertEquals(InventoryItemStatus.INACTIVE, result.status());
        assertEquals(1, result.deactivatedUnitCount());
        assertEquals(0, result.aggregatedStock().compareTo(BigDecimal.ZERO));
        assertFalse(catalogContains(created.materialCode()));
        assertFalse(itemListContains(created.inventoryItemId()));

        GetInventoryMaterialResult historical = getInventoryMaterialUseCase.execute(
                new GetInventoryMaterialQuery(created.materialCode())
        );
        assertEquals(InventoryItemStatus.INACTIVE, historical.material().status());
        assertEquals(1, historical.units().size());
        assertEquals(InventoryItemStatus.INACTIVE, historical.units().getFirst().status());
    }

    @Test
    void remainingStockRejectsDeactivationAndLeavesMaterialActive() {
        CreateInventoryItemResult created = createInk(new BigDecimal("8.0000"));

        InventoryDomainException exception = assertThrows(
                InventoryDomainException.class,
                () -> deactivate(created.materialCode())
        );
        assertTrue(exception.getMessage().contains("remaining physical stock"));
        assertTrue(catalogContains(created.materialCode()));
        assertEquals(
                InventoryItemStatus.ACTIVE,
                inventoryItemRepository.findById(created.inventoryItemId()).orElseThrow().getStatus()
        );
    }

    @Test
    void purchaseHistoryAndFinanceRemainAfterDeactivation() {
        CreateInventoryItemResult created = createInk(BigDecimal.ZERO);
        UUID purchaseId = UUID.randomUUID();
        LocalDate purchaseDate = LocalDate.of(2026, 9, 4);
        RegisterInventoryPurchaseResult purchase = registerInventoryPurchaseUseCase.execute(
                new RegisterInventoryPurchaseCommand(
                        created.inventoryItemId(),
                        purchaseId,
                        new BigDecimal("2.0000"),
                        new BigDecimal("100000.00"),
                        purchaseDate,
                        "Tinta negra",
                        new BigDecimal("200000.00")
                )
        );

        consumeInventoryMaterialUseCase.execute(new ConsumeInventoryMaterialCommand(
                created.inventoryItemId(),
                new BigDecimal("2.0000"),
                "LITER",
                InventoryMovementSourceType.PRODUCTION,
                UUID.randomUUID(),
                "consumo producción"
        ));

        FinancialTransaction before = financialTransactionRepository
                .findBySourceTypeAndSourceId(FinancialTransactionSourceType.INVENTORY_PURCHASE, purchaseId)
                .orElseThrow();

        deactivate(created.materialCode());

        FinancialTransaction after = financialTransactionRepository
                .findBySourceTypeAndSourceId(FinancialTransactionSourceType.INVENTORY_PURCHASE, purchaseId)
                .orElseThrow();
        assertEquals(before.getId(), after.getId());
        assertEquals(0, before.getAmount().getValue().compareTo(after.getAmount().getValue()));
        assertEquals(before.getTransactionDate(), after.getTransactionDate());
        assertEquals(FinancialTransactionSourceType.INVENTORY_PURCHASE, after.getSourceType());
        assertEquals(purchaseId, after.getSourceId());
        assertEquals(purchaseDate, after.getTransactionDate());
        assertEquals(0, new BigDecimal("200000.00").compareTo(after.getAmount().getValue()));

        assertTrue(inventoryMovementRepository
                .findBySourceTypeAndSourceId(InventoryMovementSourceType.PURCHASE, purchaseId)
                .isPresent());
        assertEquals(2, getInventoryMovementsUseCase.execute(
                new GetInventoryMovementsQuery(created.inventoryItemId())
        ).movements().size());
        assertEquals(purchase.financialTransactionId(), after.getId());
    }

    @Test
    void plotterHistoryRemainsValidAfterPaperDeactivation() {
        InventoryItem roll = saveIsolatedPaper(new BigDecimal("10.0000"));
        CreatePlotterJobResult job = createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                UUID.randomUUID(),
                null,
                null,
                roll.getId(),
                new BigDecimal("10.0000"),
                new BigDecimal("8000"),
                "Historial Plotter"
        ));

        deactivate(roll.getMaterialCode().getValue());

        PlotterJob persisted = plotterJobRepository.findById(job.plotterJobId()).orElseThrow();
        assertEquals(roll.getId(), persisted.getPaperInventoryItemId());
        GetInventoryItemResult historicalItem = getInventoryItemUseCase.execute(
                new GetInventoryItemQuery(roll.getId())
        );
        assertEquals(InventoryItemStatus.INACTIVE, historicalItem.status());
        assertTrue(inventoryMovementRepository
                .findBySourceTypeAndSourceId(InventoryMovementSourceType.PLOTTER, job.plotterJobId())
                .isPresent());
    }

    @Test
    void repeatedDeactivationIsIdempotent() {
        CreateInventoryItemResult created = createInk(BigDecimal.ZERO);
        DeactivateInventoryMaterialResult first = deactivate(created.materialCode());
        DeactivateInventoryMaterialResult second = deactivate(created.materialCode());

        assertEquals(first.materialCode(), second.materialCode());
        assertEquals(InventoryItemStatus.INACTIVE, second.status());
        assertEquals(1, second.deactivatedUnitCount());
    }

    @Test
    void inactiveMaterialCannotReceivePurchasesOrReusePaperCode() {
        InventoryItem firstRoll = saveIsolatedPaper(BigDecimal.ZERO);
        String inactiveCode = firstRoll.getMaterialCode().getValue();
        deactivate(inactiveCode);

        assertThrows(
                InventoryDomainException.class,
                () -> registerInventoryPurchaseUseCase.execute(new RegisterInventoryPurchaseCommand(
                        firstRoll.getId(),
                        UUID.randomUUID(),
                        new BigDecimal("1.0000"),
                        new BigDecimal("4500.00"),
                        LocalDate.of(2026, 9, 5),
                        null
                ))
        );
        assertThrows(
                InventoryDomainException.class,
                () -> increaseInventoryStockUseCase.execute(
                        new IncreaseInventoryStockCommand(firstRoll.getId(), BigDecimal.ONE)
                )
        );

        CreateInventoryItemResult secondRoll = createPaperRoll(new BigDecimal("20.0000"), new BigDecimal("4500.00"));
        assertNotEquals(inactiveCode, secondRoll.materialCode());
        assertTrue(catalogContains(secondRoll.materialCode()));
        assertFalse(catalogContains(inactiveCode));
    }

    @Test
    void paperDeactivationRequiresZeroAggregatedStockAcrossAllRolls() {
        InventoryItem first = saveIsolatedPaper(BigDecimal.ZERO);
        InventoryItem second = inventoryItemRepository.save(InventoryItem.create(
                first.getMaterialCode(),
                "Papel Plotter",
                "PAPER",
                "METER",
                new BigDecimal("5.0000"),
                new BigDecimal("1.0000"),
                "Rollo con stock",
                new BigDecimal("4500.00"),
                InventoryMaterialType.PAPER,
                uniqueRollNumber()
        ));

        assertThrows(InventoryDomainException.class, () -> deactivate(first.getMaterialCode().getValue()));
        assertEquals(
                InventoryItemStatus.ACTIVE,
                inventoryItemRepository.findById(first.getId()).orElseThrow().getStatus()
        );
        assertEquals(
                InventoryItemStatus.ACTIVE,
                inventoryItemRepository.findById(second.getId()).orElseThrow().getStatus()
        );
    }

    private DeactivateInventoryMaterialResult deactivate(String materialCode) {
        return deactivateInventoryMaterialUseCase.execute(new DeactivateInventoryMaterialCommand(materialCode));
    }

    private boolean catalogContains(String materialCode) {
        GetInventoryCatalogResult catalog = getInventoryCatalogUseCase.execute();
        return catalog.materials().stream().anyMatch(item -> materialCode.equals(item.materialCode()));
    }

    private boolean itemListContains(UUID inventoryItemId) {
        return getInventoryItemsUseCase.execute(GetInventoryItemsQuery.all()).items().stream()
                .anyMatch(item -> inventoryItemId.equals(item.inventoryItemId()));
    }

    private CreateInventoryItemResult createInk(BigDecimal stock) {
        return createInventoryItemUseCase.execute(new CreateInventoryItemCommand(
                "INK-" + UUID.randomUUID().toString().substring(0, 8),
                "Tinta negra",
                "INK",
                "LITER",
                stock,
                new BigDecimal("1.0000"),
                "Tinta",
                new BigDecimal("100000.00"),
                "INK",
                false
        ));
    }

    private CreateInventoryItemResult createPaperRoll(BigDecimal stock, BigDecimal unitCost) {
        return createInventoryItemUseCase.execute(new CreateInventoryItemCommand(
                "RP-" + UUID.randomUUID().toString().substring(0, 8),
                "Papel plotter",
                "PAPER",
                "METER",
                stock,
                new BigDecimal("1.0000"),
                "Rollo",
                unitCost,
                "PAPER",
                true
        ));
    }

    private InventoryItem saveIsolatedPaper(BigDecimal stock) {
        return inventoryItemRepository.save(InventoryItem.create(
                MaterialCode.of("PAP-" + UUID.randomUUID().toString().substring(0, 8)),
                "Papel Plotter",
                "PAPER",
                "METER",
                stock,
                new BigDecimal("1.0000"),
                "Rollo aislado",
                new BigDecimal("4500.00"),
                InventoryMaterialType.PAPER,
                uniqueRollNumber()
        ));
    }

    private static String uniqueRollNumber() {
        return "RP-A" + UUID.randomUUID().toString().substring(0, 8);
    }
}
