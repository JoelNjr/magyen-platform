package com.magyen.platform.finance.application.dto;

import com.magyen.platform.finance.domain.RecurringObligationFrequency;
import com.magyen.platform.finance.domain.RecurringObligationType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entrada del caso de uso para actualizar una obligación financiera recurrente.
 */
public record UpdateRecurringFinancialObligationCommand(
        UUID obligationId,
        String name,
        RecurringObligationType type,
        BigDecimal expectedAmount,
        RecurringObligationFrequency frequency,
        Integer dueDay,
        LocalDate startDate,
        LocalDate endDate,
        String description,
        String observation
) {
}
