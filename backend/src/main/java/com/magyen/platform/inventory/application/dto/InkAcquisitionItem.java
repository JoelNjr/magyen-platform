package com.magyen.platform.inventory.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Compra de tinta registrada en Inventario.
 */
public record InkAcquisitionItem(
        UUID purchaseId,
        UUID inventoryItemId,
        LocalDate purchaseDate,
        BigDecimal quantity,
        BigDecimal totalCost
) {
}
