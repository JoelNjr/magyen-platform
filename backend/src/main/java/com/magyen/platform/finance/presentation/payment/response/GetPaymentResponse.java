package com.magyen.platform.finance.presentation.payment.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Respuesta HTTP con el detalle de un pago.
 */
public record GetPaymentResponse(
        UUID paymentId,
        UUID orderId,
        BigDecimal amount,
        LocalDate paymentDate,
        String observations
) {
}
