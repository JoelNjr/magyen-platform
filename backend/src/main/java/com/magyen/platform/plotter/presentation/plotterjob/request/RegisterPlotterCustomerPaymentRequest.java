package com.magyen.platform.plotter.presentation.plotterjob.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Pago de la cuenta corriente. No identifica un trabajo.
 */
public record RegisterPlotterCustomerPaymentRequest(
        BigDecimal amount,
        LocalDate paymentDate,
        String observations,
        UUID registrationKey
) {
}
