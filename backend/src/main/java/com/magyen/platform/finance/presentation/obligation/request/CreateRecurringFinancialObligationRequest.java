package com.magyen.platform.finance.presentation.obligation.request;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Payload HTTP para crear una obligación financiera recurrente.
 */
public record CreateRecurringFinancialObligationRequest(
        String name,
        String type,
        BigDecimal expectedAmount,
        String frequency,
        Integer dueDay,
        LocalDate startDate,
        LocalDate endDate,
        String description,
        String observation
) {
}
