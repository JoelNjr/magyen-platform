package com.magyen.platform.production.application.dto;

import java.util.UUID;

/**
 * Consulta de un registro de mano de obra por identidad.
 */
public record GetProductionLaborWorkQuery(
        UUID productionOrderId,
        UUID laborWorkId
) {
}
