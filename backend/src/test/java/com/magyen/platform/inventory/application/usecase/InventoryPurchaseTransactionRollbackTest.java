package com.magyen.platform.inventory.application.usecase;

import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.inventory.application.dto.RegisterInventoryPurchaseCommand;
import com.magyen.platform.inventory.application.port.InventoryPurchaseFinancePort;
import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.InventoryMaterialType;
import com.magyen.platform.inventory.domain.InventoryMovementRepository;
import com.magyen.platform.inventory.domain.InventoryMovementSourceType;
import com.magyen.platform.inventory.domain.MaterialCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Si Finance falla, la compra no deja stock ni movimiento: una sola transacción.
 */
@SpringBootTest
class InventoryPurchaseTransactionRollbackTest {

    @MockitoBean
    private InventoryPurchaseFinancePort inventoryPurchaseFinancePort;

    @Autowired
    private RegisterInventoryPurchaseUseCase registerInventoryPurchaseUseCase;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID inventoryItemId;
    private UUID purchaseId;

    @AfterEach
    void deleteSyntheticTestRows() {
        if (purchaseId != null) {
            jdbcTemplate.update(
                    "DELETE FROM financial_transactions WHERE source_type = ? AND source_id = ?",
                    FinancialTransactionSourceType.INVENTORY_PURCHASE.name(),
                    purchaseId
            );
            jdbcTemplate.update(
                    "DELETE FROM inventory_movements WHERE source_type = ? AND source_id = ?",
                    InventoryMovementSourceType.PURCHASE.name(),
                    purchaseId
            );
        }
        if (inventoryItemId != null) {
            jdbcTemplate.update("DELETE FROM inventory_movements WHERE inventory_item_id = ?", inventoryItemId);
            jdbcTemplate.update("DELETE FROM inventory_items WHERE id = ?", inventoryItemId);
        }
    }

    @Test
    void financeFailureRollsBackInventoryIncreaseAndMovement() {
        InventoryItem fabric = inventoryItemRepository.save(InventoryItem.create(
                MaterialCode.of("TXRB-" + UUID.randomUUID().toString().substring(0, 8)),
                "Sudáfrica rollback",
                "FABRIC",
                "METER",
                BigDecimal.ZERO,
                null,
                null,
                null,
                InventoryMaterialType.FABRIC,
                null
        ));
        inventoryItemId = fabric.getId();
        purchaseId = UUID.randomUUID();

        when(inventoryPurchaseFinancePort.ensurePurchaseExpense(
                any(), any(), any(), any(), any(), any()
        )).thenThrow(new IllegalStateException("Finance unavailable"));

        assertThrows(IllegalStateException.class, () -> registerInventoryPurchaseUseCase.execute(
                new RegisterInventoryPurchaseCommand(
                        fabric.getId(),
                        purchaseId,
                        new BigDecimal("100.0000"),
                        new BigDecimal("10000.00"),
                        LocalDate.of(2026, 8, 16),
                        "debe revertirse"
                )
        ));

        InventoryItem reloaded = inventoryItemRepository.findById(fabric.getId()).orElseThrow();
        assertEquals(0, reloaded.getStock().compareTo(BigDecimal.ZERO));
        assertTrue(inventoryMovementRepository
                .findBySourceTypeAndSourceId(InventoryMovementSourceType.PURCHASE, purchaseId)
                .isEmpty());
        assertTrue(financialTransactionRepository
                .findBySourceTypeAndSourceId(FinancialTransactionSourceType.INVENTORY_PURCHASE, purchaseId)
                .isEmpty());
    }
}
