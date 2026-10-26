package com.magyen.platform.inventory.presentation.inventoryitem.response;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Rollo después de fijar su máquina. Stock y costo quedan iguales.
 */
public record AssignPaperRollMachineResponse(
        UUID inventoryItemId,
        String paperRollNumber,
        UUID plotterMachineId,
        BigDecimal stock,
        BigDecimal unitCost
) {
}
