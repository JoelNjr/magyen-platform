package com.magyen.platform.finance.application.dto;

import com.magyen.platform.finance.domain.RecurringObligationFrequency;
import com.magyen.platform.finance.domain.RecurringObligationType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Resultado de consulta de una obligación financiera recurrente.
 */
public record GetRecurringFinancialObligationResult(
        UUID obligationId,
        String name,
        RecurringObligationType type,
        BigDecimal expectedAmount,
        RecurringObligationFrequency frequency,
        Integer dueDay,
        LocalDate startDate,
        LocalDate endDate,
        boolean active,
        String description,
        String observation
) {
}
