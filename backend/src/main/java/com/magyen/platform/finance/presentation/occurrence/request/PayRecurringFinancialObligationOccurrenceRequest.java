package com.magyen.platform.finance.presentation.occurrence.request;

import java.time.LocalDateTime;

/**
 * Payload HTTP para pagar una ocurrencia.
 * <p>
 * {@code paymentDate} llega como texto ISO ({@code yyyy-MM-dd}) para poder
 * rechazar fechas imposibles, como 2026-02-31, en lugar de ajustarlas.
 * Omitirla conserva el comportamiento anterior para otros llamadores.
 */
public record PayRecurringFinancialObligationOccurrenceRequest(
        String paymentDate,
        LocalDateTime paidAt,
        String observation
) {
}
