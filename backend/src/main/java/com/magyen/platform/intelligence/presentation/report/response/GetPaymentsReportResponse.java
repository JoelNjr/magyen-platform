package com.magyen.platform.intelligence.presentation.report.response;

import java.math.BigDecimal;

/**
 * Respuesta HTTP del reporte de pagos.
 */
public record GetPaymentsReportResponse(
        BigDecimal totalReceived,
        long paymentCount,
        BigDecimal averagePerPayment
) {
}
