package com.magyen.platform.production.application.dto;

import java.util.List;

/**
 * Read model del historial de consumos de material de una Orden de Producción,
 * incluyendo resumen de atribución de costo histórico.
 */
public record GetProductionMaterialConsumptionsResult(
        List<GetProductionMaterialConsumptionResult> consumptions,
        ProductionMaterialCostSummary materialCostSummary
) {
}
