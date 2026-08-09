package com.magyen.platform.commercial.application.dto;

import java.math.BigDecimal;

/**
 * Representación del resumen de pago comercial de una Orden para casos de uso de consulta.
 */
public record PaymentSummaryResult(
        boolean advanceAcknowledged,
        boolean finalPaymentAcknowledged,
        BigDecimal committedTotal,
        BigDecimal remainingBalance
) {
}
