package com.magyen.platform.inventory.application.usecase;

import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemCommand;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemResult;
import com.magyen.platform.inventory.application.dto.GetPaperAcquisitionsQuery;
import com.magyen.platform.inventory.application.dto.InventoryAcquisitionCommand;
import com.magyen.platform.inventory.application.dto.RegisterInventoryPurchaseCommand;
import com.magyen.platform.inventory.domain.InventoryMovementRepository;
import com.magyen.platform.inventory.domain.InventoryMovementSourceType;
import com.magyen.platform.inventory.domain.InventoryMovementType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SPR-039 Increment I: precio de adquisición de papel y gasto único de compra.
 */
@SpringBootTest
@Transactional
class PaperAcquisitionUseCaseTest {

    private static final LocalDate PURCHASE_DATE = LocalDate.of(2026, 8, 16);

    @Autowired
    private CreateInventoryItemUseCase createInventoryItemUseCase;

    @Autowired
    private RegisterInventoryPurchaseUseCase registerInventoryPurchaseUseCase;

    @Autowired
    private GetPaperAcquisitionsUseCase getPaperAcquisitionsUseCase;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    @Test
    void oneRollTimesPricePerRollCreatesSinglePurchaseExpense() {
        UUID purchaseId = UUID.randomUUID();
        CreateInventoryItemResult roll = createPaperWithAcquisition(
                purchaseId,
                "1",
                "200000.00",
                "100.0000"
        );

        assertEquals("Papel Plotter", roll.name());
        assertTrue(roll.paperRollNumber().matches("RP-\\d{3,}"));
        assertEquals(0, new BigDecimal("100.0000").compareTo(roll.stock()));
        assertEquals(1, countPurchaseMovements(roll.inventoryItemId()));
        assertEquals(1, countPurchaseExpenses(purchaseId));
        assertEquals(new BigDecimal("200000.00"), purchaseExpenseAmount(purchaseId));
        assertEquals("PAPER", purchaseExpense(purchaseId).getCategory());
    }

    @Test
    void twoRollsTimesPricePerRollCreatesFourHundredThousandExpense() {
        UUID purchaseId = UUID.randomUUID();
        CreateInventoryItemResult roll = createPaperWithAcquisition(
                purchaseId,
                "2",
                "200000.00",
                "100.0000"
        );

        assertEquals(0, new BigDecimal("100.0000").compareTo(roll.stock()));
        assertEquals(new BigDecimal("400000.00"), purchaseExpenseAmount(purchaseId));
        assertEquals(1, countPurchaseExpenses(purchaseId));
    }

    @Test
    void threeRollsTimesPricePerRollCreatesSixHundredThousandExpense() {
        UUID purchaseId = UUID.randomUUID();
        createPaperWithAcquisition(purchaseId, "3", "200000.00", "150.0000");

        assertEquals(new BigDecimal("600000.00"), purchaseExpenseAmount(purchaseId));
        assertEquals(1, countPurchaseExpenses(purchaseId));
    }

    @Test
    void retryingSamePurchaseIdDoesNotDuplicateInventoryOrFinanceExpense() {
        UUID purchaseId = UUID.randomUUID();
        CreateInventoryItemResult roll = createPaperWithAcquisition(
                purchaseId,
                "1",
                "200000.00",
                "100.0000"
        );

        registerInventoryPurchaseUseCase.execute(new RegisterInventoryPurchaseCommand(
                roll.inventoryItemId(),
                purchaseId,
                new BigDecimal("100.0000"),
                new BigDecimal("2000.00"),
                PURCHASE_DATE,
                "reintento",
                new BigDecimal("200000.00")
        ));

        assertEquals(1, countPurchaseMovements(roll.inventoryItemId()));
        assertEquals(1, countPurchaseExpenses(purchaseId));
        assertEquals(new BigDecimal("200000.00"), purchaseExpenseAmount(purchaseId));
        assertEquals(0, new BigDecimal("100.0000").compareTo(
                inventoryMovementRepository
                        .findBySourceTypeAndSourceId(InventoryMovementSourceType.PURCHASE, purchaseId)
                        .orElseThrow()
                        .getResultingStock()
        ));
    }

    @Test
    void paperAcquisitionsInRangeUsePurchaseDateNotConsumption() {
        UUID purchaseId = UUID.randomUUID();
        CreateInventoryItemResult roll = createPaperWithAcquisition(
                purchaseId,
                "1",
                "200000.00",
                "100.0000"
        );

        var inRange = getPaperAcquisitionsUseCase.execute(
                new GetPaperAcquisitionsQuery(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31))
        );
        assertTrue(inRange.acquisitions().stream()
                .anyMatch(item -> purchaseId.equals(item.purchaseId())
                        && item.inventoryItemId().equals(roll.inventoryItemId())
                        && item.totalCost().compareTo(new BigDecimal("200000.00")) == 0));

        var outside = getPaperAcquisitionsUseCase.execute(
                new GetPaperAcquisitionsQuery(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31))
        );
        assertTrue(outside.acquisitions().stream()
                .noneMatch(item -> purchaseId.equals(item.purchaseId())));
    }

    private CreateInventoryItemResult createPaperWithAcquisition(
            UUID purchaseId,
            String rollQuantity,
            String pricePerRoll,
            String meters
    ) {
        return createInventoryItemUseCase.execute(new CreateInventoryItemCommand(
                "PAP-ACQ-" + UUID.randomUUID().toString().substring(0, 8),
                null,
                "PAPER",
                "METER",
                new BigDecimal(meters),
                new BigDecimal("10.0000"),
                null,
                null,
                "PAPER",
                true,
                new InventoryAcquisitionCommand(
                        purchaseId,
                        new BigDecimal(rollQuantity),
                        new BigDecimal(pricePerRoll),
                        null,
                        PURCHASE_DATE,
                        "compra papel increment I"
                )
        ));
    }

    private long countPurchaseMovements(UUID inventoryItemId) {
        return inventoryMovementRepository
                .findByInventoryItemIdOrderByMovementDateDesc(inventoryItemId)
                .stream()
                .filter(movement -> movement.getSourceType() == InventoryMovementSourceType.PURCHASE)
                .filter(movement -> movement.getMovementType() == InventoryMovementType.IN)
                .count();
    }

    private long countPurchaseExpenses(UUID purchaseId) {
        return financialTransactionRepository.findBySourceTypeAndSourceId(
                        FinancialTransactionSourceType.INVENTORY_PURCHASE,
                        purchaseId
                )
                .stream()
                .filter(transaction -> transaction.getType() == FinancialTransactionType.EXPENSE)
                .count();
    }

    private BigDecimal purchaseExpenseAmount(UUID purchaseId) {
        return purchaseExpense(purchaseId).getAmount().getValue();
    }

    private FinancialTransaction purchaseExpense(UUID purchaseId) {
        return financialTransactionRepository.findBySourceTypeAndSourceId(
                        FinancialTransactionSourceType.INVENTORY_PURCHASE,
                        purchaseId
                )
                .orElseThrow();
    }
}
