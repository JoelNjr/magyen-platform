package com.magyen.platform.plotter.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Trabajo externo cuyo pago supera el total. No se presenta como deuda.
 */
public record PlotterNegativeBalanceItem(
        UUID plotterJobId,
        UUID customerId,
        String customerName,
        LocalDate creationDate,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal outstandingAmount
) {
}
