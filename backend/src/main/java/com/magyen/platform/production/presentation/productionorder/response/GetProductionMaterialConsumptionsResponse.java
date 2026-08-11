package com.magyen.platform.production.presentation.productionorder.response;

import java.util.List;

/**
 * Respuesta HTTP del historial de consumos de material de una Orden de Producción.
 */
public record GetProductionMaterialConsumptionsResponse(
        List<GetProductionMaterialConsumptionResponse> consumptions,
        ProductionMaterialCostSummaryResponse materialCostSummary
) {
}
