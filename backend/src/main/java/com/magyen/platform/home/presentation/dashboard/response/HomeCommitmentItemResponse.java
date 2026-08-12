package com.magyen.platform.home.presentation.dashboard.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Ítem HTTP de compromiso financiero PENDING.
 */
public record HomeCommitmentItemResponse(
        UUID occurrenceId,
        UUID obligationId,
        String name,
        String type,
        BigDecimal expectedAmount,
        LocalDate dueDate,
        String status,
        boolean overdue,
        Integer daysUntilDue,
        Integer daysOverdue
) {
}
