package com.magyen.platform.inventory.application.dto;

import java.util.UUID;

/**
 * Asignación única de un rollo sin máquina a una máquina activa.
 */
public record AssignPaperRollMachineCommand(
        UUID inventoryItemId,
        UUID plotterMachineId
) {
}
