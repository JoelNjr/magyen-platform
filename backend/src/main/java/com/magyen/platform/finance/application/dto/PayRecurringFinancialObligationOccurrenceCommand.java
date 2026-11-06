package com.magyen.platform.finance.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entrada del caso de uso para pagar una ocurrencia pendiente.
 * <p>
 * {@code paymentDate} es la fecha real del pago. Si viene informada, el ledger
 * y {@code paidDate} usan esa fecha y no se sustituye por la fecha actual.
 * Si se omite, {@code paidAt} conserva el comportamiento anterior; si también
 * se omite, se usa la marca de tiempo actual para quienes ya dependían de ella.
 */
public record PayRecurringFinancialObligationOccurrenceCommand(
        UUID occurrenceId,
        LocalDate paymentDate,
        LocalDateTime paidAt,
        String observation
) {
    public PayRecurringFinancialObligationOccurrenceCommand(UUID occurrenceId) {
        this(occurrenceId, null, null, null);
    }

    public PayRecurringFinancialObligationOccurrenceCommand(
            UUID occurrenceId,
            LocalDateTime paidAt,
            String observation
    ) {
        this(occurrenceId, null, paidAt, observation);
    }
}
