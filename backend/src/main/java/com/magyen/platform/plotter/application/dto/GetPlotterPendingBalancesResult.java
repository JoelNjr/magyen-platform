package com.magyen.platform.plotter.application.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Saldos pendientes all-time de trabajos externos de Plotter.
 * <p>
 * {@code externalBilledAmount} y {@code externalPaidAmount} cubren todos los
 * trabajos externos no cancelados, incluidos los ya saldados.
 * {@code outstandingAmount} suma solo saldos positivos.
 */
public record GetPlotterPendingBalancesResult(
        List<PlotterCustomerPendingBalance> customers,
        int openJobCount,
        int customerCount,
        BigDecimal externalBilledAmount,
        BigDecimal externalPaidAmount,
        BigDecimal outstandingAmount,
        List<PlotterNegativeBalanceItem> negativeBalances
) {
}
