package com.magyen.platform.plotter.application.dto;

import com.magyen.platform.plotter.domain.PlotterJobStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Trabajo externo con saldo pendiente. El saldo se calcula al leer.
 */
public record PlotterPendingJobBalance(
        UUID plotterJobId,
        LocalDate creationDate,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal outstandingAmount,
        PlotterJobStatus status
) {
}
