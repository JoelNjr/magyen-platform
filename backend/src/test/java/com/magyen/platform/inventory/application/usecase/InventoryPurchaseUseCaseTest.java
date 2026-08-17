package com.magyen.platform.inventory.application.usecase;

import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.inventory.application.dto.ConsumeInventoryMaterialCommand;
import com.magyen.platform.inventory.application.dto.ConsumeInventoryMaterialResult;
import com.magyen.platform.inventory.application.dto.RegisterInventoryPurchaseCommand;
import com.magyen.platform.inventory.application.dto.RegisterInventoryPurchaseResult;
import com.magyen.platform.inventory.application.dto.UpdateInventoryUnitCostCommand;
import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.InventoryMaterialType;
import com.magyen.platform.inventory.domain.InventoryMovement;
import com.magyen.platform.inventory.domain.InventoryMovementRepository;
import com.magyen.platform.inventory.domain.InventoryMovementSourceType;
import com.magyen.platform.inventory.domain.InventoryMovementType;
import com.magyen.platform.inventory.domain.MaterialCode;
import com.magyen.platform.inventory.domain.exception.InventoryDomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SPR-038 Increment B: compra de inventario + gasto de caja, sin duplicar el gasto al consumir.
 */
@SpringBootTest
@Transactional
class InventoryPurchaseUseCaseTest {

    @Autowired
    private RegisterInventoryPurchaseUseCase registerInventoryPurchaseUseCase;

    @Autowired
    private ConsumeInventoryMaterialUseCase consumeInventoryMaterialUseCase;

    @Autowired
    private UpdateInventoryUnitCostUseCase updateInventoryUnitCostUseCase;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    private InventoryItem fabric;

    @BeforeEach
    void setUp() {
        fabric = inventoryItemRepository.save(InventoryItem.create(
                MaterialCode.of("PUR-" + UUID.randomUUID().toString().substring(0, 8)),
                "Sudáfrica",
                "FABRIC",
                "METER",
                BigDecimal.ZERO,
                null,
                null,
                null,
                InventoryMaterialType.FABRIC,
                null
        ));
    }

    @Test
    void purchaseCreatesInventoryInAndExactlyOneFinanceExpense() {
        UUID purchaseId = UUID.randomUUID();

        RegisterInventoryPurchaseResult result = purchase(
                fabric.getId(),
                purchaseId,
                "100.0000",
                "10000.00"
        );

        assertFalse(result.alreadyProcessed());
        assertEquals(purchaseId, result.purchaseId());
        assertEquals(new BigDecimal("100.0000"), result.quantity());
        assertEquals(new BigDecimal("10000.00"), result.unitCost());
        assertEquals(new BigDecimal("1000000.00"), result.totalCost());
        assertEquals(new BigDecimal("100.0000"), result.resultingStock());
        assertEquals("MATERIALS", result.financeCategory());

        InventoryMovement movement = inventoryMovementRepository
                .findBySourceTypeAndSourceId(InventoryMovementSourceType.PURCHASE, purchaseId)
                .orElseThrow();
        assertEquals(InventoryMovementType.IN, movement.getMovementType());
        assertEquals(new BigDecimal("100.0000"), movement.getQuantity());
        assertEquals(new BigDecimal("10000.00"), movement.getUnitCost());
        assertEquals(new BigDecimal("1000000.00"), movement.getTotalCost());
        assertEquals(purchaseId, movement.getSourceId());

        FinancialTransaction expense = financialTransactionRepository
                .findBySourceTypeAndSourceId(FinancialTransactionSourceType.INVENTORY_PURCHASE, purchaseId)
                .orElseThrow();
        assertEquals(FinancialTransactionType.EXPENSE, expense.getType());
        assertEquals(new BigDecimal("1000000.00"), expense.getAmount().getValue());
        assertEquals("MATERIALS", expense.getCategory());
        assertEquals(result.financialTransactionId(), expense.getId());

        assertEquals(1, countPurchaseExpensesFor(fabric.getId()));
        assertEquals(new BigDecimal("100.0000"),
                inventoryItemRepository.findById(fabric.getId()).orElseThrow().getStock());
    }

    @Test
    void retryWithSamePurchaseIdDoesNotDuplicateInventoryOrFinance() {
        UUID purchaseId = UUID.randomUUID();
        RegisterInventoryPurchaseCommand command = new RegisterInventoryPurchaseCommand(
                fabric.getId(),
                purchaseId,
                new BigDecimal("100.0000"),
                new BigDecimal("10000.00"),
                LocalDate.of(2026, 8, 16),
                "reintento"
        );

        RegisterInventoryPurchaseResult first = registerInventoryPurchaseUseCase.execute(command);
        RegisterInventoryPurchaseResult second = registerInventoryPurchaseUseCase.execute(command);

        assertFalse(first.alreadyProcessed());
        assertTrue(second.alreadyProcessed());
        assertEquals(first.purchaseId(), second.purchaseId());
        assertEquals(first.movementId(), second.movementId());
        assertEquals(first.financialTransactionId(), second.financialTransactionId());
        assertEquals(new BigDecimal("1000000.00"), second.totalCost());

        List<InventoryMovement> movements = inventoryMovementRepository
                .findByInventoryItemIdOrderByMovementDateDesc(fabric.getId());
        assertEquals(1, movements.size());
        assertEquals(new BigDecimal("100.0000"),
                inventoryItemRepository.findById(fabric.getId()).orElseThrow().getStock());

        long expenses = financialTransactionRepository.findAllNewestFirst().stream()
                .filter(transaction -> transaction.getSourceType() == FinancialTransactionSourceType.INVENTORY_PURCHASE)
                .filter(transaction -> purchaseId.equals(transaction.getSourceId()))
                .count();
        assertEquals(1, expenses);
    }

    @Test
    void multiplePurchasesOfSameMaterialCreateIndependentExpenses() {
        UUID firstPurchaseId = UUID.randomUUID();
        UUID secondPurchaseId = UUID.randomUUID();

        RegisterInventoryPurchaseResult first = purchase(fabric.getId(), firstPurchaseId, "100.0000", "10000.00");
        RegisterInventoryPurchaseResult second = purchase(fabric.getId(), secondPurchaseId, "50.0000", "12000.00");

        assertEquals(new BigDecimal("1000000.00"), first.totalCost());
        assertEquals(new BigDecimal("600000.00"), second.totalCost());
        assertEquals(new BigDecimal("150.0000"), second.resultingStock());
        assertEquals(new BigDecimal("12000.00"),
                inventoryItemRepository.findById(fabric.getId()).orElseThrow().getUnitCost());

        assertEquals(2, inventoryMovementRepository
                .findByInventoryItemIdOrderByMovementDateDesc(fabric.getId())
                .size());

        FinancialTransaction firstExpense = financialTransactionRepository
                .findBySourceTypeAndSourceId(FinancialTransactionSourceType.INVENTORY_PURCHASE, firstPurchaseId)
                .orElseThrow();
        FinancialTransaction secondExpense = financialTransactionRepository
                .findBySourceTypeAndSourceId(FinancialTransactionSourceType.INVENTORY_PURCHASE, secondPurchaseId)
                .orElseThrow();
        assertEquals(new BigDecimal("1000000.00"), firstExpense.getAmount().getValue());
        assertEquals(new BigDecimal("600000.00"), secondExpense.getAmount().getValue());
        assertTrue(!firstExpense.getId().equals(secondExpense.getId()));
    }

    @Test
    void rejectsZeroAndNegativeQuantityAndUnitCost() {
        UUID purchaseId = UUID.randomUUID();

        assertThrows(InventoryDomainException.class, () -> purchase(
                fabric.getId(), purchaseId, "0", "10000.00"
        ));
        assertThrows(InventoryDomainException.class, () -> purchase(
                fabric.getId(), UUID.randomUUID(), "-1.0000", "10000.00"
        ));
        assertThrows(InventoryDomainException.class, () -> purchase(
                fabric.getId(), UUID.randomUUID(), "1.0000", "0"
        ));
        assertThrows(InventoryDomainException.class, () -> purchase(
                fabric.getId(), UUID.randomUUID(), "1.0000", "-1.00"
        ));

        assertTrue(inventoryMovementRepository
                .findByInventoryItemIdOrderByMovementDateDesc(fabric.getId())
                .isEmpty());
        assertTrue(financialTransactionRepository
                .findBySourceTypeAndSourceId(FinancialTransactionSourceType.INVENTORY_PURCHASE, purchaseId)
                .isEmpty());
        assertEquals(0, inventoryItemRepository.findById(fabric.getId()).orElseThrow().getStock()
                .compareTo(BigDecimal.ZERO));
    }

    @Test
    void rejectsUnknownInventoryItem() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> purchase(UUID.randomUUID(), UUID.randomUUID(), "1.0000", "10000.00")
        );
        assertTrue(exception.getMessage().contains("Inventory item not found"));
    }

    @Test
    void consumptionAfterPurchaseDecreasesStockUsesHistoricalCostAndCreatesNoFinanceExpense() {
        UUID purchaseId = UUID.randomUUID();
        purchase(fabric.getId(), purchaseId, "100.0000", "10000.00");
        long expensesAfterPurchase = countAllExpenses();

        UUID consumptionId = UUID.randomUUID();
        ConsumeInventoryMaterialResult consumed = consumeInventoryMaterialUseCase.execute(
                new ConsumeInventoryMaterialCommand(
                        fabric.getId(),
                        new BigDecimal("6.5000"),
                        "METER",
                        InventoryMovementSourceType.PRODUCTION,
                        consumptionId,
                        "consumo producción"
                )
        );

        assertEquals(new BigDecimal("93.5000"), consumed.resultingStock());
        assertEquals(new BigDecimal("10000.00"), consumed.unitCost());
        assertEquals(new BigDecimal("65000.00"), consumed.totalCost());
        assertEquals("Sudáfrica", consumed.materialName());
        assertEquals(expensesAfterPurchase, countAllExpenses());
        assertTrue(financialTransactionRepository
                .findBySourceTypeAndSourceId(FinancialTransactionSourceType.PRODUCTION, consumptionId)
                .isEmpty());
        assertTrue(financialTransactionRepository
                .findBySourceTypeAndSourceId(FinancialTransactionSourceType.INVENTORY_PURCHASE, purchaseId)
                .isPresent());

        updateInventoryUnitCostUseCase.execute(new UpdateInventoryUnitCostCommand(
                fabric.getId(),
                new BigDecimal("18000.00")
        ));

        InventoryMovement historicalOut = inventoryMovementRepository
                .findBySourceTypeAndSourceId(InventoryMovementSourceType.PRODUCTION, consumptionId)
                .orElseThrow();
        assertEquals(new BigDecimal("10000.00"), historicalOut.getUnitCost());
        assertEquals(new BigDecimal("65000.00"), historicalOut.getTotalCost());
        assertEquals(new BigDecimal("18000.00"),
                inventoryItemRepository.findById(fabric.getId()).orElseThrow().getUnitCost());
    }

    @Test
    void consumptionCannotExceedStockPurchased() {
        purchase(fabric.getId(), UUID.randomUUID(), "5.0000", "10000.00");

        assertThrows(InventoryDomainException.class, () -> consumeInventoryMaterialUseCase.execute(
                new ConsumeInventoryMaterialCommand(
                        fabric.getId(),
                        new BigDecimal("6.5000"),
                        "METER",
                        InventoryMovementSourceType.PRODUCTION,
                        UUID.randomUUID(),
                        null
                )
        ));

        assertEquals(new BigDecimal("5.0000"),
                inventoryItemRepository.findById(fabric.getId()).orElseThrow().getStock());
        assertEquals(1, inventoryMovementRepository
                .findByInventoryItemIdOrderByMovementDateDesc(fabric.getId())
                .size());
        assertEquals(1, countPurchaseExpensesFor(fabric.getId()));
    }

    @Test
    void paperPurchaseUsesPaperExpenseCategory() {
        InventoryItem paper = inventoryItemRepository.save(InventoryItem.create(
                MaterialCode.of("PAP-" + UUID.randomUUID().toString().substring(0, 8)),
                "Papel sublimación",
                "PAPER",
                "METER",
                BigDecimal.ZERO,
                null,
                null,
                null,
                InventoryMaterialType.PAPER,
                null
        ));
        UUID purchaseId = UUID.randomUUID();

        RegisterInventoryPurchaseResult result = purchase(paper.getId(), purchaseId, "10.0000", "2500.00");

        assertEquals("PAPER", result.financeCategory());
        assertEquals("PAPER", financialTransactionRepository
                .findBySourceTypeAndSourceId(FinancialTransactionSourceType.INVENTORY_PURCHASE, purchaseId)
                .orElseThrow()
                .getCategory());
    }

    private RegisterInventoryPurchaseResult purchase(
            UUID inventoryItemId,
            UUID purchaseId,
            String quantity,
            String unitCost
    ) {
        return registerInventoryPurchaseUseCase.execute(new RegisterInventoryPurchaseCommand(
                inventoryItemId,
                purchaseId,
                new BigDecimal(quantity),
                new BigDecimal(unitCost),
                LocalDate.of(2026, 8, 16),
                "compra de prueba"
        ));
    }

    private long countAllExpenses() {
        return financialTransactionRepository.findAllNewestFirst().stream()
                .filter(transaction -> transaction.getType() == FinancialTransactionType.EXPENSE)
                .count();
    }

    private long countPurchaseExpensesFor(UUID inventoryItemId) {
        return inventoryMovementRepository
                .findByInventoryItemIdOrderByMovementDateDesc(inventoryItemId)
                .stream()
                .filter(movement -> movement.getSourceType() == InventoryMovementSourceType.PURCHASE)
                .map(InventoryMovement::getSourceId)
                .distinct()
                .map(purchaseId -> financialTransactionRepository.findBySourceTypeAndSourceId(
                        FinancialTransactionSourceType.INVENTORY_PURCHASE,
                        purchaseId
                ))
                .filter(java.util.Optional::isPresent)
                .count();
    }
}
