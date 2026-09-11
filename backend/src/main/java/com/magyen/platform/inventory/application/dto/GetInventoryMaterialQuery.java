package com.magyen.platform.inventory.application.dto;

/**
 * Consulta del detalle de un material de catálogo por su código de negocio.
 */
public record GetInventoryMaterialQuery(
        String materialCode
) {
}
