package com.magyen.platform.finance.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entrada para registrar/garantizar el ingreso del ledger de un pago de Plotter.
 */
public record RegisterPlotterPaymentIncomeCommand(
        UUID plotterPaymentId,
        BigDecimal amount,
        LocalDate paymentDate,
        String observation
) {
}
