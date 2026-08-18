package com.magyen.platform.inventory.application.usecase;

import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemCommand;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemResult;
import com.magyen.platform.inventory.application.dto.InventoryAcquisitionCommand;
import com.magyen.platform.inventory.application.dto.RegisterInventoryPurchaseCommand;
import com.magyen.platform.inventory.domain.InventoryMaterialType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class InventoryMaterialIdentityUseCaseTest {

    @Autowired
    private CreateInventoryItemUseCase createInventoryItemUseCase;

    @Autowired
    private RegisterInventoryPurchaseUseCase registerInventoryPurchaseUseCase;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    @Test
    void paperCreationDoesNotRequireNameOrSalePriceAndSharesMaterialCode() {
        CreateInventoryItemResult first = createInventoryItemUseCase.execute(
                new CreateInventoryItemCommand(
                        null,
                        null,
                        null,
                        "UNIT",
                        new BigDecimal("50.0000"),
                        null,
                        null,
                        null,
                        "PAPER",
                        true
                )
        );
        CreateInventoryItemResult second = createInventoryItemUseCase.execute(
                new CreateInventoryItemCommand(
                        null,
                        null,
                        null,
                        "UNIT",
                        new BigDecimal("40.0000"),
                        null,
                        null,
                        null,
                        "PAPER",
                        true
                )
        );

        assertEquals(InventoryMaterialType.PAPER, first.materialType());
        assertEquals("Papel Plotter", first.name());
        assertNull(first.unitCost());
        assertTrue(first.plotterPaperRoll());
        assertNotNull(first.paperRollNumber());
        assertTrue(first.paperRollNumber().matches("RP-\\d{3,}"));
        assertEquals(first.materialCode(), second.materialCode());
        assertTrue(!first.paperRollNumber().equals(second.paperRollNumber()));
    }

    @Test
    void generatedMaterialCodesAreConsecutiveBusinessCodes() {
        CreateInventoryItemResult first = createInventoryItemUseCase.execute(
                new CreateInventoryItemCommand(
                        "ignored-1",
                        "Tinta F",
                        "INK",
                        "LITER",
                        new BigDecimal("1.0000"),
                        null,
                        null,
                        null,
                        "INK",
                        false
                )
        );
        CreateInventoryItemResult second = createInventoryItemUseCase.execute(
                new CreateInventoryItemCommand(
                        "ignored-2",
                        "Hilo F",
                        "THREAD",
                        "UNIT",
                        new BigDecimal("1.0000"),
                        null,
                        null,
                        null,
                        "THREAD",
                        false
                )
        );

        assertTrue(first.materialCode().matches("MAT-\\d{3,}"));
        assertTrue(second.materialCode().matches("MAT-\\d{3,}"));
        assertTrue(!first.materialCode().equals(second.materialCode()));
    }

    @Test
    void fabricPurchaseCostPerMeterCreatesExactlyOneFinanceExpense() {
        UUID purchaseId = UUID.randomUUID();

        CreateInventoryItemResult result = createInventoryItemUseCase.execute(
                new CreateInventoryItemCommand(
                        null,
                        "Tela compra F",
                        "FABRIC",
                        "METER",
                        BigDecimal.ZERO,
                        null,
                        null,
                        null,
                        "FABRIC",
                        false,
                        new InventoryAcquisitionCommand(
                                purchaseId,
                                new BigDecimal("10.0000"),
                                new BigDecimal("12000.00"),
                                null,
                                LocalDate.of(2026, 8, 17),
                                "Compra inicial tela"
                        )
                )
        );

        assertEquals(new BigDecimal("10.0000"), result.stock());
        assertEquals(new BigDecimal("12000.00"), result.unitCost());

        FinancialTransaction expense = financialTransactionRepository
                .findBySourceTypeAndSourceId(FinancialTransactionSourceType.INVENTORY_PURCHASE, purchaseId)
                .orElseThrow();
        assertEquals(FinancialTransactionType.EXPENSE, expense.getType());
        assertEquals(new BigDecimal("120000.00"), expense.getAmount().getValue());

        registerInventoryPurchaseUseCase.execute(new RegisterInventoryPurchaseCommand(
                result.inventoryItemId(),
                purchaseId,
                new BigDecimal("10.0000"),
                new BigDecimal("12000.00"),
                LocalDate.of(2026, 8, 17),
                "retry"
        ));

        long expenses = financialTransactionRepository.findAllNewestFirst().stream()
                .filter(transaction -> transaction.getSourceType() == FinancialTransactionSourceType.INVENTORY_PURCHASE)
                .filter(transaction -> purchaseId.equals(transaction.getSourceId()))
                .count();
        assertEquals(1, expenses);
    }

    @Test
    void nonFabricAcquisitionUsesTotalPurchaseCost() {
        UUID purchaseId = UUID.randomUUID();

        CreateInventoryItemResult result = createInventoryItemUseCase.execute(
                new CreateInventoryItemCommand(
                        null,
                        "Tinta adquisición F",
                        "INK",
                        "LITER",
                        BigDecimal.ZERO,
                        null,
                        null,
                        null,
                        "INK",
                        false,
                        new InventoryAcquisitionCommand(
                                purchaseId,
                                new BigDecimal("2.0000"),
                                null,
                                new BigDecimal("80000.00"),
                                LocalDate.of(2026, 8, 17),
                                "Compra tinta"
                        )
                )
        );

        assertEquals(new BigDecimal("2.0000"), result.stock());
        assertEquals(new BigDecimal("40000.00"), result.unitCost());

        FinancialTransaction expense = financialTransactionRepository
                .findBySourceTypeAndSourceId(FinancialTransactionSourceType.INVENTORY_PURCHASE, purchaseId)
                .orElseThrow();
        assertEquals(new BigDecimal("80000.00"), expense.getAmount().getValue());
    }
}
