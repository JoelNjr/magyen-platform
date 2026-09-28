package com.magyen.platform.plotter.application.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Deuda de trabajos externos de Plotter agrupada por el mes de {@code creationDate}.
 * <p>
 * Los importes corresponden solo a trabajos con saldo positivo de ese mes.
 * No es un saldo persistido.
 */
public record PlotterMonthPendingBalance(
        int year,
        int month,
        List<PlotterCustomerPendingBalance> customers,
        int openJobCount,
        int customerCount,
        BigDecimal billedAmount,
        BigDecimal paidAmount,
        BigDecimal outstandingAmount
) {
}
