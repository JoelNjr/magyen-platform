package com.magyen.platform.inventory.presentation.inventoryitem.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Compra inicial opcional al crear un material.
 */
public record InventoryAcquisitionRequest(
        UUID purchaseId,
        BigDecimal quantity,
        BigDecimal unitCost,
        BigDecimal totalCost,
        LocalDate purchaseDate,
        String observation
) {
}
