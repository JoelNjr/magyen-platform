package com.magyen.platform.plotter.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Resultado de un pago individual de Plotter.
 */
public record GetPlotterPaymentResult(
        UUID paymentId,
        UUID plotterJobId,
        BigDecimal amount,
        LocalDate paymentDate,
        String observations
) {
}
