package com.magyen.platform.production.presentation.productionorder.response;

import java.util.List;

/**
 * Respuesta HTTP del historial de mano de obra con resumen.
 */
public record GetProductionLaborWorksResponse(
        List<GetProductionLaborWorkResponse> laborWorks,
        ProductionLaborCostSummaryResponse laborCostSummary
) {
}
