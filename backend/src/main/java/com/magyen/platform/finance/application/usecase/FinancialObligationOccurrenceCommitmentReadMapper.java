package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.FinancialObligationOccurrenceCommitmentResult;
import com.magyen.platform.finance.domain.RecurringFinancialObligation;
import com.magyen.platform.finance.domain.RecurringFinancialObligationOccurrence;
import com.magyen.platform.finance.domain.RecurringObligationType;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Enriquecimiento de ocurrencias PENDING para lecturas de compromiso.
 */
final class FinancialObligationOccurrenceCommitmentReadMapper {

    private FinancialObligationOccurrenceCommitmentReadMapper() {
    }

    static FinancialObligationOccurrenceCommitmentResult toCommitment(
            RecurringFinancialObligationOccurrence occurrence,
            Map<UUID, RecurringFinancialObligation> obligationsById,
            LocalDate today
    ) {
        Objects.requireNonNull(occurrence, "Occurrence must not be null");
        Objects.requireNonNull(obligationsById, "Obligations map must not be null");
        Objects.requireNonNull(today, "Today must not be null");

        RecurringFinancialObligation obligation = obligationsById.get(occurrence.getRecurringObligationId());
        String obligationName = obligation != null ? obligation.getName() : "Unknown obligation";
        RecurringObligationType obligationType =
                obligation != null ? obligation.getType() : RecurringObligationType.OTHER;

        boolean overdue = occurrence.getDueDate().isBefore(today);
        Integer daysUntilDue = null;
        Integer daysOverdue = null;
        if (overdue) {
            daysOverdue = (int) ChronoUnit.DAYS.between(occurrence.getDueDate(), today);
        } else {
            daysUntilDue = (int) ChronoUnit.DAYS.between(today, occurrence.getDueDate());
        }

        return new FinancialObligationOccurrenceCommitmentResult(
                occurrence.getId(),
                occurrence.getRecurringObligationId(),
                obligationName,
                obligationType,
                occurrence.getDueDate(),
                occurrence.getExpectedAmount().getValue(),
                occurrence.getStatus(),
                overdue,
                daysUntilDue,
                daysOverdue
        );
    }
}
