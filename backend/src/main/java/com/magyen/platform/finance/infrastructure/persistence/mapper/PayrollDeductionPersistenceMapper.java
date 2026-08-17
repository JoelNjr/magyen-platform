package com.magyen.platform.finance.infrastructure.persistence.mapper;

import com.magyen.platform.finance.domain.FinancialAmount;
import com.magyen.platform.finance.domain.PayrollDeduction;
import com.magyen.platform.finance.infrastructure.persistence.entity.PayrollDeductionEntity;

import java.util.Objects;

/**
 * Convierte entre el agregado de dominio {@link PayrollDeduction} y su modelo JPA.
 */
public class PayrollDeductionPersistenceMapper {

    public PayrollDeductionEntity toEntity(PayrollDeduction deduction) {
        Objects.requireNonNull(deduction, "Payroll deduction must not be null");

        PayrollDeductionEntity entity = new PayrollDeductionEntity();
        entity.setId(deduction.getId());
        entity.setEmployeeId(deduction.getEmployeeId());
        entity.setType(deduction.getType());
        entity.setAmount(deduction.getAmount().getValue());
        entity.setDeductionDate(deduction.getDeductionDate());
        entity.setDescription(deduction.getDescription());
        entity.setStatus(deduction.getStatus());
        entity.setCreatedAt(deduction.getCreatedAt());
        return entity;
    }

    public PayrollDeduction toDomain(PayrollDeductionEntity entity) {
        Objects.requireNonNull(entity, "Payroll deduction entity must not be null");

        return PayrollDeduction.reconstitute(
                entity.getId(),
                entity.getEmployeeId(),
                entity.getType(),
                FinancialAmount.of(entity.getAmount()),
                entity.getDeductionDate(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}
