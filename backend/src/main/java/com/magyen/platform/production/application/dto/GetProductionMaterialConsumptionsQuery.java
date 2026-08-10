package com.magyen.platform.production.application.dto;

import java.util.UUID;

/**
 * Consulta del historial de consumos de material de una Orden de Producción.
 */
public record GetProductionMaterialConsumptionsQuery(
        UUID productionOrderId
) {
}
