package com.magyen.platform.plotter.application.dto;

import com.magyen.platform.plotter.domain.PlotterProfitabilityScope;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Resumen analítico de Plotter. No crea transacciones Finance.
 * <p>
 * {@code inkCost} es la suma de adquisiciones de tinta en el período, no un consumo por trabajo.
 * {@code totalPaperCost} es la suma de adquisiciones de papel en el período, no de OUTs.
 * {@code analyticalPlotterResult} es ingreso combinado menos adquisiciones de papel y tinta.
 * {@code externalPaidAmount} y {@code externalOutstandingAmount} son cobranza de trabajos EXTERNAL
 * del período (pagos reales), no costo de papel ni de tinta.
 */
public record GetPlotterProfitabilityResult(
        LocalDate fromDate,
        LocalDate toDate,
        PlotterProfitabilityScope scope,
        int jobCount,
        int externalJobCount,
        int internalJobCount,
        int wasteJobCount,
        BigDecimal totalPaperPrintedMeters,
        BigDecimal wastePrintedMeters,
        BigDecimal externalRevenue,
        BigDecimal internalRevenue,
        BigDecimal combinedRevenue,
        BigDecimal externalPaperCost,
        BigDecimal internalPaperCost,
        BigDecimal totalPaperCost,
        BigDecimal internalPaperPrintedMeters,
        int unvaluedPaperJobCount,
        boolean paperCostComplete,
        boolean inkCostRecorded,
        BigDecimal inkCost,
        BigDecimal analyticalPlotterResult,
        BigDecimal externalPaidAmount,
        BigDecimal externalOutstandingAmount,
        List<PlotterInternalOrderCostItem> internalOrders
) {
}
