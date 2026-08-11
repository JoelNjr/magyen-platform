package com.magyen.platform.production.application.dto;

import java.util.List;

/**
 * Resultado del historial de mano de obra con resumen de costo.
 */
public record GetProductionLaborWorksResult(
        List<GetProductionLaborWorkResult> laborWorks,
        ProductionLaborCostSummary laborCostSummary
) {
}
