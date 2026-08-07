package com.magyen.platform.finance.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entrada del caso de uso para registrar un pago sobre una Orden.
 */
public record RegisterPaymentCommand(
        UUID orderId,
        BigDecimal amount,
        LocalDate paymentDate,
        String observations
) {
}
