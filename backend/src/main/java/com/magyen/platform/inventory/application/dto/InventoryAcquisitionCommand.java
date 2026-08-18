package com.magyen.platform.inventory.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Compra inicial opcional al crear un material (Increment B).
 * <p>
 * Tela: {@code unitCost} es el costo por metro.
 * Otros no-papel: {@code totalCost} es el desembolso total.
 */
public record InventoryAcquisitionCommand(
        UUID purchaseId,
        BigDecimal quantity,
        BigDecimal unitCost,
        BigDecimal totalCost,
        LocalDate purchaseDate,
        String observation
) {
}
