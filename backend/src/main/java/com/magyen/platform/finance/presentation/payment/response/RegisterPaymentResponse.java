package com.magyen.platform.finance.presentation.payment.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Respuesta HTTP tras registrar un pago exitosamente.
 */
public record RegisterPaymentResponse(
        UUID paymentId,
        UUID orderId,
        BigDecimal amount,
        LocalDate paymentDate,
        String observations
) {
}
