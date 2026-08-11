package com.magyen.platform.finance.infrastructure.persistence.mapper;

import com.magyen.platform.finance.domain.FinancialAmount;
import com.magyen.platform.finance.domain.RecurringFinancialObligationOccurrence;
import com.magyen.platform.finance.infrastructure.persistence.entity.RecurringFinancialObligationOccurrenceEntity;

import java.util.Objects;

/**
 * Convierte entre el agregado de dominio y su modelo JPA.
 */
public class RecurringFinancialObligationOccurrencePersistenceMapper {

    public RecurringFinancialObligationOccurrenceEntity toEntity(
            RecurringFinancialObligationOccurrence occurrence
    ) {
        Objects.requireNonNull(occurrence, "Occurrence must not be null");

        RecurringFinancialObligationOccurrenceEntity entity =
                new RecurringFinancialObligationOccurrenceEntity();
        entity.setId(occurrence.getId());
        entity.setRecurringObligationId(occurrence.getRecurringObligationId());
        entity.setDueDate(occurrence.getDueDate());
        entity.setExpectedAmount(occurrence.getExpectedAmount().getValue());
        entity.setStatus(occurrence.getStatus());
        entity.setPaidDate(occurrence.getPaidDate());
        entity.setFinancialTransactionId(occurrence.getFinancialTransactionId());
        entity.setObservation(occurrence.getObservation());
        return entity;
    }

    public RecurringFinancialObligationOccurrence toDomain(
            RecurringFinancialObligationOccurrenceEntity entity
    ) {
        Objects.requireNonNull(entity, "Occurrence entity must not be null");

        return RecurringFinancialObligationOccurrence.reconstitute(
                entity.getId(),
                entity.getRecurringObligationId(),
                entity.getDueDate(),
                FinancialAmount.of(entity.getExpectedAmount()),
                entity.getStatus(),
                entity.getPaidDate(),
                entity.getFinancialTransactionId(),
                entity.getObservation()
        );
    }
}
