package com.magyen.platform.plotter.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Pago de la cuenta corriente de un cliente externo. No selecciona un trabajo.
 */
public record RegisterPlotterCustomerPaymentCommand(
        UUID customerId,
        BigDecimal amount,
        LocalDate paymentDate,
        String observations,
        UUID registrationKey
) {
}
