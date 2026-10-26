package com.magyen.platform.inventory.presentation.inventoryitem.request;

import java.util.UUID;

/**
 * Asignación administrativa única de un rollo sin máquina.
 */
public record AssignPaperRollMachineRequest(UUID plotterMachineId) {
}
