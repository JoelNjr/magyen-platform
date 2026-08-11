package com.magyen.platform.production.application.dto;

import java.math.BigDecimal;

/**
 * Resumen de atribución de costo de materiales de una Orden de Producción.
 * <p>
 * {@code totalMaterialCost} suma solo consumos valorizados (snapshot histórico).
 * Es null cuando no hay ningún consumo con totalCost conocido.
 * Nunca inventa cero a partir de costos ausentes.
 */
public record ProductionMaterialCostSummary(
        BigDecimal totalMaterialCost,
        int consumptionCount,
        int valuedConsumptionCount,
        int unvaluedConsumptionCount
) {
}
