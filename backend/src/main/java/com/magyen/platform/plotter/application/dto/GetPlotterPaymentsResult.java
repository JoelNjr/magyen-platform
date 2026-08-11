package com.magyen.platform.plotter.application.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resultado de listado de pagos de un trabajo de Plotter.
 */
public record GetPlotterPaymentsResult(
        List<GetPlotterPaymentResult> payments,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal outstandingAmount
) {
}
