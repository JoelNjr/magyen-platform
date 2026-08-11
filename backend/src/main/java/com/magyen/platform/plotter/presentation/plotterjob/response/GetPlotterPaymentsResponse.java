package com.magyen.platform.plotter.presentation.plotterjob.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Respuesta HTTP del listado de pagos de un trabajo de Plotter.
 */
public record GetPlotterPaymentsResponse(
        List<GetPlotterPaymentResponse> payments,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal outstandingAmount
) {
}
