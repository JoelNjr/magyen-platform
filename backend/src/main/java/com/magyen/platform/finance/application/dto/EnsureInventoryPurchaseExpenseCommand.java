package com.magyen.platform.finance.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entrada para garantizar el EXPENSE de caja de una compra de inventario.
 */
public record EnsureInventoryPurchaseExpenseCommand(
        UUID purchaseId,
        BigDecimal amount,
        LocalDate purchaseDate,
        String category,
        String description,
        String observation
) {
}
