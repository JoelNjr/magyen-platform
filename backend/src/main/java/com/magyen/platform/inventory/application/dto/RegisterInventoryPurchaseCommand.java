package com.magyen.platform.inventory.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entrada para registrar una compra/recepción de material.
 * <p>
 * {@code purchaseId} es opcional: si se reenvía el mismo identificador, la operación es idempotente.
 */
public record RegisterInventoryPurchaseCommand(
        UUID inventoryItemId,
        UUID purchaseId,
        BigDecimal quantity,
        BigDecimal unitCost,
        LocalDate purchaseDate,
        String observation
) {
}
