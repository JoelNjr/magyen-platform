package com.magyen.platform.finance.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de persistencia para {@link RecurringFinancialObligationOccurrence}.
 */
public interface RecurringFinancialObligationOccurrenceRepository {

    RecurringFinancialObligationOccurrence save(RecurringFinancialObligationOccurrence occurrence);

    Optional<RecurringFinancialObligationOccurrence> findById(UUID id);

    Optional<RecurringFinancialObligationOccurrence> findByRecurringObligationIdAndDueDate(
            UUID recurringObligationId,
            LocalDate dueDate
    );

    /**
     * Lista todas las ocurrencias ordenadas por fecha de vencimiento y luego por id.
     */
    List<RecurringFinancialObligationOccurrence> findAll();

    /**
     * Lista ocurrencias por estado, ordenadas por fecha de vencimiento y luego por id.
     */
    List<RecurringFinancialObligationOccurrence> findByStatus(RecurringObligationOccurrenceStatus status);

    /**
     * Lista ocurrencias con dueDate en {@code [fromDate, toDate]}, ordenadas por dueDate e id.
     */
    List<RecurringFinancialObligationOccurrence> findByDueDateBetween(LocalDate fromDate, LocalDate toDate);

    /**
     * PENDING ordenadas por dueDate ASC, expectedAmount DESC, id ASC.
     */
    List<RecurringFinancialObligationOccurrence> findPendingOrdered();

    /**
     * PENDING con {@code dueDate < beforeDate}, mismo orden de compromiso.
     */
    List<RecurringFinancialObligationOccurrence> findPendingDueBefore(LocalDate beforeDate);

    /**
     * PENDING con dueDate en {@code [fromDate, toDate]}, mismo orden de compromiso.
     */
    List<RecurringFinancialObligationOccurrence> findPendingDueBetween(LocalDate fromDate, LocalDate toDate);

    /**
     * Suma de expectedAmount de todas las ocurrencias PENDING.
     */
    BigDecimal sumPendingExpectedAmount();

    /**
     * Suma de expectedAmount de PENDING con {@code dueDate < beforeDate}.
     */
    BigDecimal sumPendingExpectedAmountDueBefore(LocalDate beforeDate);
}
