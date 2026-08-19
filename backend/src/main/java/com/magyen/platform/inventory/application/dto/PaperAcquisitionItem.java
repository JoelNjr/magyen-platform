package com.magyen.platform.inventory.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Compra de rollo de papel registrada en Inventario.
 */
public record PaperAcquisitionItem(
        UUID purchaseId,
        UUID inventoryItemId,
        LocalDate purchaseDate,
        BigDecimal quantity,
        BigDecimal totalCost
) {
}
