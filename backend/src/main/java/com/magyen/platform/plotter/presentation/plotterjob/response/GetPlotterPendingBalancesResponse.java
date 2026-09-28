package com.magyen.platform.plotter.presentation.plotterjob.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Deuda de trabajos externos de Plotter. No es un saldo almacenado.
 * {@code months} agrupa por la fecha del trabajo.
 */
public record GetPlotterPendingBalancesResponse(
        List<CustomerBalanceResponse> customers,
        List<MonthBalanceResponse> months,
        int openJobCount,
        int customerCount,
        BigDecimal externalBilledAmount,
        BigDecimal externalPaidAmount,
        BigDecimal outstandingAmount,
        List<NegativeBalanceResponse> negativeBalances
) {
    public record MonthBalanceResponse(
            int year,
            int month,
            List<CustomerBalanceResponse> customers,
            int openJobCount,
            int customerCount,
            BigDecimal billedAmount,
            BigDecimal paidAmount,
            BigDecimal outstandingAmount
    ) {
    }

    public record CustomerBalanceResponse(
            UUID customerId,
            String customerName,
            int openJobCount,
            BigDecimal billedAmount,
            BigDecimal paidAmount,
            BigDecimal outstandingAmount,
            List<JobBalanceResponse> jobs
    ) {
    }

    public record JobBalanceResponse(
            UUID plotterJobId,
            LocalDate creationDate,
            BigDecimal totalAmount,
            BigDecimal paidAmount,
            BigDecimal outstandingAmount,
            String status
    ) {
    }

    public record NegativeBalanceResponse(
            UUID plotterJobId,
            UUID customerId,
            String customerName,
            LocalDate creationDate,
            BigDecimal totalAmount,
            BigDecimal paidAmount,
            BigDecimal outstandingAmount
    ) {
    }
}
