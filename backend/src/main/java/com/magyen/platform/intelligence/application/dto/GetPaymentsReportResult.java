package com.magyen.platform.intelligence.application.dto;

import java.math.BigDecimal;

/**
 * Resultado del caso de uso de reporte de pagos.
 */
public record GetPaymentsReportResult(
        BigDecimal totalReceived,
        long paymentCount,
        BigDecimal averagePerPayment
) {
}
