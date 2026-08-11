package com.magyen.platform.plotter.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entrada del caso de uso para registrar un pago sobre un trabajo de Plotter.
 */
public record RegisterPlotterPaymentCommand(
        UUID plotterJobId,
        BigDecimal amount,
        LocalDate paymentDate,
        String observations
) {
}
