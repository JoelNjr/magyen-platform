package com.magyen.platform.plotter.application.dto;

import java.math.BigDecimal;

/**
 * Costo de Plotter interno atribuible a una orden.
 * <p>
 * {@code plotterMaterialCost} es el snapshot físico de papel.
 * {@code internalPlotterServiceCost} es el valor del servicio (metros × precio por metro).
 * {@code attributablePlotterCost} evita doble conteo: prefiere el servicio y, si no hay,
 * usa el papel físico (trabajos históricos sin valor de servicio).
 */
public record GetInternalPlotterOrderCostsResult(
        BigDecimal plotterMaterialCost,
        int internalJobCount,
        int valuedJobCount,
        int unvaluedJobCount,
        boolean plotterCostAttributable,
        BigDecimal internalPlotterServiceCost,
        BigDecimal attributablePlotterCost
) {
}
