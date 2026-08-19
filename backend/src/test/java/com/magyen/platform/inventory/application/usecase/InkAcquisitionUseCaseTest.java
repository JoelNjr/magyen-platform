package com.magyen.platform.inventory.application.usecase;

import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemCommand;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemResult;
import com.magyen.platform.inventory.application.dto.GetInkAcquisitionsQuery;
import com.magyen.platform.inventory.application.dto.InventoryAcquisitionCommand;
import com.magyen.platform.inventory.application.dto.RegisterInventoryPurchaseCommand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class InkAcquisitionUseCaseTest {

    private static final LocalDate PURCHASE_DATE = LocalDate.of(2099, 5, 12);

    @Autowired
    private CreateInventoryItemUseCase createInventoryItemUseCase;

    @Autowired
    private RegisterInventoryPurchaseUseCase registerInventoryPurchaseUseCase;

    @Autowired
    private GetInkAcquisitionsUseCase getInkAcquisitionsUseCase;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    @Test
    void oneInkPurchaseCreatesSingleExpenseAndAppearsInPeriod() {
        UUID purchaseId = UUID.randomUUID();
        CreateInventoryItemResult ink = createInkWithAcquisition(purchaseId, "100000.00");

        assertEquals(1, countPurchaseExpenses(purchaseId));
        var inRange = getInkAcquisitionsUseCase.execute(
                new GetInkAcquisitionsQuery(LocalDate.of(2099, 5, 1), LocalDate.of(2099, 5, 31))
        );
        assertTrue(inRange.acquisitions().stream().anyMatch(item ->
                purchaseId.equals(item.purchaseId())
                        && ink.inventoryItemId().equals(item.inventoryItemId())
                        && item.totalCost().compareTo(new BigDecimal("100000.00")) == 0
        ));
    }

    @Test
    void multipleInkPurchasesAccumulateAndRespectDateFilter() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        UUID outside = UUID.randomUUID();
        createInkWithAcquisition(first, "100000.00");
        createInkWithAcquisition(second, "150000.00");
        createInventoryItemUseCase.execute(new CreateInventoryItemCommand(
                "INK-OUT-" + UUID.randomUUID().toString().substring(0, 8),
                "Tinta fuera",
                "INK",
                "LITER",
                new BigDecimal("1.0000"),
                new BigDecimal("0.1000"),
                null,
                null,
                "INK",
                false,
                new InventoryAcquisitionCommand(
                        outside,
                        BigDecimal.ONE,
                        new BigDecimal("90000.00"),
                        null,
                        LocalDate.of(2099, 4, 10),
                        "tinta abril"
                )
        ));

        var may = getInkAcquisitionsUseCase.execute(
                new GetInkAcquisitionsQuery(LocalDate.of(2099, 5, 1), LocalDate.of(2099, 5, 31))
        );
        BigDecimal mayTotal = may.acquisitions().stream()
                .filter(item -> first.equals(item.purchaseId()) || second.equals(item.purchaseId()))
                .map(item -> item.totalCost())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("250000.00"), mayTotal.setScale(2));

        var april = getInkAcquisitionsUseCase.execute(
                new GetInkAcquisitionsQuery(LocalDate.of(2099, 4, 1), LocalDate.of(2099, 4, 30))
        );
        assertTrue(april.acquisitions().stream().anyMatch(item -> outside.equals(item.purchaseId())));
        assertTrue(april.acquisitions().stream().noneMatch(item -> first.equals(item.purchaseId())));
    }

    @Test
    void retryingSameInkPurchaseDoesNotDuplicateFinanceExpense() {
        UUID purchaseId = UUID.randomUUID();
        CreateInventoryItemResult ink = createInkWithAcquisition(purchaseId, "100000.00");

        registerInventoryPurchaseUseCase.execute(new RegisterInventoryPurchaseCommand(
                ink.inventoryItemId(),
                purchaseId,
                new BigDecimal("1.0000"),
                new BigDecimal("100000.00"),
                PURCHASE_DATE,
                "reintento tinta",
                new BigDecimal("100000.00")
        ));

        assertEquals(1, countPurchaseExpenses(purchaseId));
    }

    private CreateInventoryItemResult createInkWithAcquisition(UUID purchaseId, String totalCost) {
        return createInventoryItemUseCase.execute(new CreateInventoryItemCommand(
                "INK-ACQ-" + UUID.randomUUID().toString().substring(0, 8),
                "Tinta Plotter",
                "INK",
                "LITER",
                new BigDecimal("1.0000"),
                new BigDecimal("0.1000"),
                null,
                null,
                "INK",
                false,
                new InventoryAcquisitionCommand(
                        purchaseId,
                        BigDecimal.ONE,
                        new BigDecimal(totalCost),
                        null,
                        PURCHASE_DATE,
                        "compra tinta"
                )
        ));
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
}
