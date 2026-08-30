package com.magyen.platform.production.application.dto;

import java.math.BigDecimal;

/**
 * Resumen de costos directos adicionales. {@code totalOtherCost} es null si no hay registros.
 */
public record ProductionOtherCostSummary(
        BigDecimal totalOtherCost,
        int otherCostCount
) {
}
