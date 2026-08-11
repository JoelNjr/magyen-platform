package com.magyen.platform.production.application.dto;

import java.util.UUID;

/**
 * Consulta del historial de mano de obra de una Orden de Producción.
 */
public record GetProductionLaborWorksQuery(
        UUID productionOrderId
) {
}
