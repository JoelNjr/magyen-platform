package com.magyen.platform.finance.presentation.obligation.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Representación HTTP de una obligación financiera recurrente.
 */
public record RecurringFinancialObligationResponse(
        UUID obligationId,
        String name,
        String type,
        BigDecimal expectedAmount,
        String frequency,
        Integer dueDay,
        LocalDate startDate,
        LocalDate endDate,
        boolean active,
        String description,
        String observation
) {
}
