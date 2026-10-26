package com.magyen.platform.inventory.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Rollo después de fijar su máquina. El stock y el costo no cambian.
 */
public record AssignPaperRollMachineResult(
        UUID inventoryItemId,
        String paperRollNumber,
        UUID plotterMachineId,
        BigDecimal stock,
        BigDecimal unitCost
) {
}
