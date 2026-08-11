package com.magyen.platform.finance.infrastructure.persistence.repository;

import com.magyen.platform.finance.domain.RecurringFinancialObligationOccurrence;
import com.magyen.platform.finance.domain.RecurringFinancialObligationOccurrenceRepository;
import com.magyen.platform.finance.domain.RecurringObligationOccurrenceStatus;
import com.magyen.platform.finance.infrastructure.persistence.entity.RecurringFinancialObligationOccurrenceEntity;
import com.magyen.platform.finance.infrastructure.persistence.mapper.RecurringFinancialObligationOccurrencePersistenceMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de infraestructura del port {@link RecurringFinancialObligationOccurrenceRepository}.
 */
@Repository
public class JpaRecurringFinancialObligationOccurrenceRepository
        implements RecurringFinancialObligationOccurrenceRepository {

    private final SpringDataRecurringFinancialObligationOccurrenceRepository springDataRepository;
    private final RecurringFinancialObligationOccurrencePersistenceMapper persistenceMapper;

    public JpaRecurringFinancialObligationOccurrenceRepository(
            SpringDataRecurringFinancialObligationOccurrenceRepository springDataRepository,
            RecurringFinancialObligationOccurrencePersistenceMapper persistenceMapper
    ) {
        this.springDataRepository = Objects.requireNonNull(
                springDataRepository,
                "Spring Data occurrence repository must not be null"
        );
        this.persistenceMapper = Objects.requireNonNull(
                persistenceMapper,
                "Occurrence persistence mapper must not be null"
        );
    }

    @Override
    public RecurringFinancialObligationOccurrence save(RecurringFinancialObligationOccurrence occurrence) {
        Objects.requireNonNull(occurrence, "Occurrence must not be null");

        RecurringFinancialObligationOccurrenceEntity entity = persistenceMapper.toEntity(occurrence);
        RecurringFinancialObligationOccurrenceEntity saved = springDataRepository.save(entity);
        return persistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<RecurringFinancialObligationOccurrence> findById(UUID id) {
        Objects.requireNonNull(id, "Occurrence id must not be null");
        return springDataRepository.findById(id).map(persistenceMapper::toDomain);
    }

    @Override
    public Optional<RecurringFinancialObligationOccurrence> findByRecurringObligationIdAndDueDate(
            UUID recurringObligationId,
            LocalDate dueDate
    ) {
        Objects.requireNonNull(recurringObligationId, "Recurring obligation id must not be null");
        Objects.requireNonNull(dueDate, "Due date must not be null");

        return springDataRepository
                .findByRecurringObligationIdAndDueDate(recurringObligationId, dueDate)
                .map(persistenceMapper::toDomain);
    }

    @Override
    public List<RecurringFinancialObligationOccurrence> findAll() {
        return springDataRepository.findAllByOrderByDueDateAscIdAsc().stream()
                .map(persistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<RecurringFinancialObligationOccurrence> findByStatus(
            RecurringObligationOccurrenceStatus status
    ) {
        Objects.requireNonNull(status, "Status must not be null");
        return springDataRepository.findByStatusOrderByDueDateAscIdAsc(status).stream()
                .map(persistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<RecurringFinancialObligationOccurrence> findByDueDateBetween(
            LocalDate fromDate,
            LocalDate toDate
    ) {
        Objects.requireNonNull(fromDate, "From date must not be null");
        Objects.requireNonNull(toDate, "To date must not be null");
        return springDataRepository
                .findByDueDateBetweenOrderByDueDateAscIdAsc(fromDate, toDate)
                .stream()
                .map(persistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<RecurringFinancialObligationOccurrence> findPendingOrdered() {
        return springDataRepository
                .findByStatusOrderByDueDateAscExpectedAmountDescIdAsc(
                        RecurringObligationOccurrenceStatus.PENDING
                )
                .stream()
                .map(persistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<RecurringFinancialObligationOccurrence> findPendingDueBefore(LocalDate beforeDate) {
        Objects.requireNonNull(beforeDate, "Before date must not be null");
        return springDataRepository
                .findByStatusAndDueDateLessThanOrderByDueDateAscExpectedAmountDescIdAsc(
                        RecurringObligationOccurrenceStatus.PENDING,
                        beforeDate
                )
                .stream()
                .map(persistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<RecurringFinancialObligationOccurrence> findPendingDueBetween(
            LocalDate fromDate,
            LocalDate toDate
    ) {
        Objects.requireNonNull(fromDate, "From date must not be null");
        Objects.requireNonNull(toDate, "To date must not be null");
        return springDataRepository
                .findByStatusAndDueDateBetweenOrderByDueDateAscExpectedAmountDescIdAsc(
                        RecurringObligationOccurrenceStatus.PENDING,
                        fromDate,
                        toDate
                )
                .stream()
                .map(persistenceMapper::toDomain)
                .toList();
    }

    @Override
    public BigDecimal sumPendingExpectedAmount() {
        return normalizeMoney(
                springDataRepository.sumExpectedAmountByStatus(RecurringObligationOccurrenceStatus.PENDING)
        );
    }

    @Override
    public BigDecimal sumPendingExpectedAmountDueBefore(LocalDate beforeDate) {
        Objects.requireNonNull(beforeDate, "Before date must not be null");
        return normalizeMoney(
                springDataRepository.sumExpectedAmountByStatusAndDueDateBefore(
                        RecurringObligationOccurrenceStatus.PENDING,
                        beforeDate
                )
        );
    }

    private static BigDecimal normalizeMoney(BigDecimal amount) {
        BigDecimal value = amount == null ? BigDecimal.ZERO : amount;
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
