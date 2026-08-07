package com.magyen.platform.finance.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Resultado del caso de uso de consulta de un pago.
 */
public record GetPaymentResult(
        UUID paymentId,
        UUID orderId,
        BigDecimal amount,
        LocalDate paymentDate,
        String observations
) {
}
