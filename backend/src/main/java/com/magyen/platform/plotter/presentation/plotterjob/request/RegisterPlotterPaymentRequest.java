package com.magyen.platform.plotter.presentation.plotterjob.request;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Payload HTTP para registrar un pago sobre un trabajo de Plotter.
 */
public record RegisterPlotterPaymentRequest(
        BigDecimal amount,
        LocalDate paymentDate,
        String observations
) {
}
