package com.magyen.platform.inventory.application.dto;

/**
 * Desactiva un material de inventario activo identificado por {@code materialCode}.
 */
public record DeactivateInventoryMaterialCommand(
        String materialCode
) {
}
