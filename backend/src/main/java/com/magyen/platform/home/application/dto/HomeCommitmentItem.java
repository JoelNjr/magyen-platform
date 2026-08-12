package com.magyen.platform.home.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Ítem de compromiso financiero PENDING en el Dashboard Home.
 */
public record HomeCommitmentItem(
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
