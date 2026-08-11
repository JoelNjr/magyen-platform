package com.magyen.platform.plotter.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Resultado del registro de un pago de Plotter.
 */
public record RegisterPlotterPaymentResult(
        UUID paymentId,
        UUID plotterJobId,
        BigDecimal amount,
        LocalDate paymentDate,
        String observations,
        BigDecimal paidAmount,
        BigDecimal outstandingAmount
) {
}
