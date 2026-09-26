package com.magyen.platform.plotter.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Deuda de un cliente por trabajos externos de Plotter aún abiertos.
 * <p>
 * Los importes del cliente corresponden solo a esos trabajos abiertos.
 */
public record PlotterCustomerPendingBalance(
        UUID customerId,
        String customerName,
        int openJobCount,
        BigDecimal billedAmount,
        BigDecimal paidAmount,
        BigDecimal outstandingAmount,
        List<PlotterPendingJobBalance> jobs
) {
}
