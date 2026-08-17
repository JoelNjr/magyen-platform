package com.magyen.platform.inventory.presentation.inventoryitem.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Respuesta HTTP tras registrar una entrada/compra de material.
 */
public record RegisterInventoryPurchaseResponse(
        UUID purchaseId,
        UUID inventoryItemId,
        String materialName,
        String materialCode,
        UUID movementId,
        UUID financialTransactionId,
        BigDecimal quantity,
        String unitOfMeasure,
        BigDecimal unitCost,
        BigDecimal totalCost,
        BigDecimal resultingStock,
        LocalDate purchaseDate,
        LocalDateTime movementDate,
        String observation,
        String financeCategory,
        boolean alreadyProcessed
) {
}
