package com.magyen.platform.plotter.application.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Saldos pendientes de trabajos externos de Plotter.
 * <p>
 * {@code customers} sigue siendo el acumulado histórico.
 * {@code months} divide ese pendiente por el mes de la fecha del trabajo.
 * {@code externalBilledAmount} y {@code externalPaidAmount} cubren todos los
 * trabajos externos no cancelados, incluidos los ya saldados.
 * {@code outstandingAmount} suma solo saldos positivos.
 */
public record GetPlotterPendingBalancesResult(
        List<PlotterCustomerPendingBalance> customers,
        List<PlotterMonthPendingBalance> months,
        int openJobCount,
        int customerCount,
        BigDecimal externalBilledAmount,
        BigDecimal externalPaidAmount,
        BigDecimal outstandingAmount,
        List<PlotterNegativeBalanceItem> negativeBalances
) {
}
