package com.magyen.platform.finance.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Garantiza el par EXPENSE+INCOME del servicio Plotter interno.
 * <p>
 * Ambos movimientos usan {@code sourceId = plotterJobId} y sourceTypes distintos.
 */
public record EnsurePlotterInternalServiceLedgerCommand(
        UUID plotterJobId,
        BigDecimal amount,
        LocalDate transactionDate,
        String observation
) {
}
