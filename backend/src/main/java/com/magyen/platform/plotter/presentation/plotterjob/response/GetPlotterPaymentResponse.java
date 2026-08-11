package com.magyen.platform.plotter.presentation.plotterjob.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Respuesta HTTP de un pago individual de Plotter.
 */
public record GetPlotterPaymentResponse(
        UUID paymentId,
        UUID plotterJobId,
        BigDecimal amount,
        LocalDate paymentDate,
        String observations
) {
}
