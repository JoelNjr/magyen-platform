package com.magyen.platform.plotter.application.dto;

import com.magyen.platform.plotter.domain.PlotterProfitabilityScope;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Resumen analítico de Plotter. No crea transacciones Finance.
 * <p>
 * {@code inkCostRecorded} es false en V1: Plotter no registra consumo de tinta.
 * {@code analyticalPlotterResult} es ingreso externo menos costo de papel externo valorado;
 * queda null si falta valorizar papel de algún trabajo externo del período.
 */
public record GetPlotterProfitabilityResult(
        LocalDate fromDate,
        LocalDate toDate,
        PlotterProfitabilityScope scope,
        int jobCount,
        int externalJobCount,
        int internalJobCount,
        BigDecimal totalPaperPrintedMeters,
        BigDecimal externalRevenue,
        BigDecimal externalPaperCost,
        BigDecimal internalPaperCost,
        BigDecimal totalPaperCost,
        BigDecimal internalPaperPrintedMeters,
        int unvaluedPaperJobCount,
        boolean paperCostComplete,
        boolean inkCostRecorded,
        BigDecimal inkCost,
        BigDecimal analyticalPlotterResult,
        List<PlotterInternalOrderCostItem> internalOrders
) {
}
