package com.magyen.platform.inventory.application.port;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Puerto de salida Inventory → Finance para el gasto de caja de una compra de material.
 * <p>
 * El consumo de producción no usa este puerto: el material ya se reconoció al comprar.
 */
public interface InventoryPurchaseFinancePort {

    InventoryPurchaseFinanceRecord ensurePurchaseExpense(
            UUID purchaseId,
            BigDecimal amount,
            LocalDate purchaseDate,
            String category,
            String description,
            String observation
    );

    record InventoryPurchaseFinanceRecord(
            UUID financialTransactionId,
            BigDecimal amount,
            String category,
            boolean alreadyProcessed
    ) {
    }
}
