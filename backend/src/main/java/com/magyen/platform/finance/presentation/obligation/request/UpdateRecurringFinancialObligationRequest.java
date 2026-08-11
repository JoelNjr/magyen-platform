package com.magyen.platform.finance.presentation.obligation.request;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Payload HTTP para actualizar una obligación financiera recurrente.
 */
public record UpdateRecurringFinancialObligationRequest(
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
