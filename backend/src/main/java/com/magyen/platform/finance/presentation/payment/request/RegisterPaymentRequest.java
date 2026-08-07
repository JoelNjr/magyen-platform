package com.magyen.platform.finance.presentation.payment.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Payload HTTP para registrar un pago sobre una Orden.
 */
public record RegisterPaymentRequest(
        UUID orderId,
        BigDecimal amount,
        LocalDate paymentDate,
        String observations
) {
}
