package com.magyen.platform.plotter.presentation.plotterjob.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Deuda all-time de trabajos externos de Plotter. No es un saldo almacenado.
 */
public record GetPlotterPendingBalancesResponse(
        List<CustomerBalanceResponse> customers,
        int openJobCount,
        int customerCount,
        BigDecimal externalBilledAmount,
        BigDecimal externalPaidAmount,
        BigDecimal outstandingAmount,
        List<NegativeBalanceResponse> negativeBalances
) {
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
