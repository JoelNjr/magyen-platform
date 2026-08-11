package com.magyen.platform.finance.application.dto;

import java.time.LocalDate;

/**
 * Entrada del caso de uso de generación controlada de ocurrencias.
 */
public record GenerateRecurringFinancialObligationOccurrencesCommand(
        LocalDate fromDate,
        LocalDate toDate
) {
}
