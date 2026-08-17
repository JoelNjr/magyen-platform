package com.magyen.platform.plotter.application.dto;

import java.math.BigDecimal;

/**
 * Acumulación de costo histórico de papel de Plotter interno.
 * <p>
 * Los trabajos sin snapshot de costo no se tratan como cero: incrementan {@code unvaluedJobCount}.
 */
public record GetInternalPlotterOrderCostsResult(
        BigDecimal plotterMaterialCost,
        int internalJobCount,
        int valuedJobCount,
        int unvaluedJobCount,
        boolean plotterCostAttributable
) {
}
