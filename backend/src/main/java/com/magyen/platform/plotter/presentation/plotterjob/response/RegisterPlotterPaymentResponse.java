package com.magyen.platform.plotter.presentation.plotterjob.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Respuesta HTTP del registro de un pago de Plotter.
 */
public record RegisterPlotterPaymentResponse(
        UUID paymentId,
        UUID plotterJobId,
        BigDecimal amount,
        LocalDate paymentDate,
        String observations,
        BigDecimal paidAmount,
        BigDecimal outstandingAmount
) {
}
