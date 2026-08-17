package com.magyen.platform.inventory.presentation.inventoryitem.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Payload HTTP para registrar una entrada/compra de material.
 * <p>
 * El total lo calcula el servidor. {@code purchaseId} permite reintentos idempotentes.
 */
public record RegisterInventoryPurchaseRequest(
        UUID purchaseId,
        BigDecimal quantity,
        BigDecimal unitCost,
        LocalDate purchaseDate,
        String observation
) {
}
