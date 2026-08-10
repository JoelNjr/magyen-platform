package com.magyen.platform.production.application.dto;

import java.util.List;

/**
 * Read model del historial de consumos de material de una Orden de Producción.
 */
public record GetProductionMaterialConsumptionsResult(
        List<GetProductionMaterialConsumptionResult> consumptions
) {
}
