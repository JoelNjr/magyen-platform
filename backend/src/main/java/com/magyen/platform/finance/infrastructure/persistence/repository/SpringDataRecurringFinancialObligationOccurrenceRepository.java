package com.magyen.platform.finance.infrastructure.persistence.repository;

import com.magyen.platform.finance.domain.RecurringObligationOccurrenceStatus;
import com.magyen.platform.finance.infrastructure.persistence.entity.RecurringFinancialObligationOccurrenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para ocurrencias de obligaciones recurrentes.
 */
public interface SpringDataRecurringFinancialObligationOccurrenceRepository
        extends JpaRepository<RecurringFinancialObligationOccurrenceEntity, UUID> {

    Optional<RecurringFinancialObligationOccurrenceEntity> findByRecurringObligationIdAndDueDate(
            UUID recurringObligationId,
            LocalDate dueDate
    );

    List<RecurringFinancialObligationOccurrenceEntity> findAllByOrderByDueDateAscIdAsc();

    List<RecurringFinancialObligationOccurrenceEntity> findByStatusOrderByDueDateAscIdAsc(
            RecurringObligationOccurrenceStatus status
    );

    List<RecurringFinancialObligationOccurrenceEntity> findByDueDateBetweenOrderByDueDateAscIdAsc(
            LocalDate fromDate,
            LocalDate toDate
    );

    List<RecurringFinancialObligationOccurrenceEntity>
    findByStatusOrderByDueDateAscExpectedAmountDescIdAsc(
            RecurringObligationOccurrenceStatus status
    );

    List<RecurringFinancialObligationOccurrenceEntity>
    findByStatusAndDueDateLessThanOrderByDueDateAscExpectedAmountDescIdAsc(
            RecurringObligationOccurrenceStatus status,
            LocalDate beforeDate
    );

    List<RecurringFinancialObligationOccurrenceEntity>
    findByStatusAndDueDateBetweenOrderByDueDateAscExpectedAmountDescIdAsc(
            RecurringObligationOccurrenceStatus status,
            LocalDate fromDate,
            LocalDate toDate
    );

    @Query("""
            select coalesce(sum(occurrence.expectedAmount), 0)
            from RecurringFinancialObligationOccurrenceEntity occurrence
            where occurrence.status = :status
            """)
    BigDecimal sumExpectedAmountByStatus(@Param("status") RecurringObligationOccurrenceStatus status);

    @Query("""
            select coalesce(sum(occurrence.expectedAmount), 0)
            from RecurringFinancialObligationOccurrenceEntity occurrence
            where occurrence.status = :status
              and occurrence.dueDate < :beforeDate
            """)
    BigDecimal sumExpectedAmountByStatusAndDueDateBefore(
            @Param("status") RecurringObligationOccurrenceStatus status,
            @Param("beforeDate") LocalDate beforeDate
    );
}
