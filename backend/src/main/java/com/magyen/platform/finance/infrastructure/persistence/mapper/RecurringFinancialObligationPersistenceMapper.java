package com.magyen.platform.finance.infrastructure.persistence.mapper;

import com.magyen.platform.finance.domain.FinancialAmount;
import com.magyen.platform.finance.domain.RecurringFinancialObligation;
import com.magyen.platform.finance.infrastructure.persistence.entity.RecurringFinancialObligationEntity;

import java.util.Objects;

/**
 * Convierte entre el agregado de dominio {@link RecurringFinancialObligation} y su modelo JPA.
 */
public class RecurringFinancialObligationPersistenceMapper {

    public RecurringFinancialObligationEntity toEntity(RecurringFinancialObligation obligation) {
        Objects.requireNonNull(obligation, "Obligation must not be null");

        RecurringFinancialObligationEntity entity = new RecurringFinancialObligationEntity();
        entity.setId(obligation.getId());
        entity.setName(obligation.getName());
        entity.setObligationType(obligation.getType());
        entity.setExpectedAmount(obligation.getExpectedAmount().getValue());
        entity.setFrequency(obligation.getFrequency());
        entity.setDueDay(obligation.getDueDay());
        entity.setStartDate(obligation.getStartDate());
        entity.setEndDate(obligation.getEndDate());
        entity.setActive(obligation.isActive());
        entity.setDescription(obligation.getDescription());
        entity.setObservation(obligation.getObservation());
        return entity;
    }

    public RecurringFinancialObligation toDomain(RecurringFinancialObligationEntity entity) {
        Objects.requireNonNull(entity, "Obligation entity must not be null");

        return RecurringFinancialObligation.reconstitute(
                entity.getId(),
                entity.getName(),
                entity.getObligationType(),
                FinancialAmount.of(entity.getExpectedAmount()),
                entity.getFrequency(),
                entity.getDueDay(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.isActive(),
                entity.getDescription(),
                entity.getObservation()
        );
    }
}
