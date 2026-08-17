package com.magyen.platform.inventory.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Resultado de una compra/recepción de inventario.
 * <p>
 * El total lo calcula el servidor: {@code quantity × unitCost}.
 */
public record RegisterInventoryPurchaseResult(
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
