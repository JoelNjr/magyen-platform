package com.magyen.platform.finance.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Resultado idempotente del par de asientos del servicio Plotter interno.
 */
public record EnsurePlotterInternalServiceLedgerResult(
        UUID plotterJobId,
        UUID expenseTransactionId,
        UUID incomeTransactionId,
        BigDecimal amount,
        boolean alreadyProcessed
) {
}
