package com.magyen.platform.finance.infrastructure.persistence.mapper;

import com.magyen.platform.finance.domain.FinancialAmount;
import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.infrastructure.persistence.entity.FinancialTransactionEntity;

import java.util.Objects;

/**
 * Convierte entre el agregado de dominio {@link FinancialTransaction} y su modelo JPA.
 */
public class FinancialTransactionPersistenceMapper {

    public FinancialTransactionEntity toEntity(FinancialTransaction financialTransaction) {
        Objects.requireNonNull(financialTransaction, "Financial transaction must not be null");

        FinancialTransactionEntity entity = new FinancialTransactionEntity();
        entity.setId(financialTransaction.getId());
        entity.setTransactionType(financialTransaction.getType());
        entity.setAmount(financialTransaction.getAmount().getValue());
        entity.setTransactionDate(financialTransaction.getTransactionDate());
        entity.setCategory(financialTransaction.getCategory());
        entity.setDescription(financialTransaction.getDescription());
        entity.setObservation(financialTransaction.getObservation());
        entity.setSourceType(financialTransaction.getSourceType());
        entity.setSourceId(financialTransaction.getSourceId());
        return entity;
    }

    public FinancialTransaction toDomain(FinancialTransactionEntity entity) {
        Objects.requireNonNull(entity, "Financial transaction entity must not be null");

        FinancialTransactionSourceType sourceType = entity.getSourceType() == null
                ? FinancialTransactionSourceType.MANUAL
                : entity.getSourceType();

        return FinancialTransaction.reconstitute(
                entity.getId(),
                entity.getTransactionType(),
                FinancialAmount.of(entity.getAmount()),
                entity.getTransactionDate(),
                entity.getCategory(),
                entity.getDescription(),
                entity.getObservation(),
                sourceType,
                entity.getSourceId()
        );
    }
}
