package com.magyen.platform.finance.infrastructure.persistence.mapper;

import com.magyen.platform.finance.domain.FinancialAmount;
import com.magyen.platform.finance.domain.PayrollEmployee;
import com.magyen.platform.finance.infrastructure.persistence.entity.PayrollEmployeeEntity;

import java.util.Objects;

/**
 * Convierte entre el agregado de dominio {@link PayrollEmployee} y su modelo JPA.
 */
public class PayrollEmployeePersistenceMapper {

    public PayrollEmployeeEntity toEntity(PayrollEmployee employee) {
        Objects.requireNonNull(employee, "Payroll employee must not be null");

        PayrollEmployeeEntity entity = new PayrollEmployeeEntity();
        entity.setId(employee.getId());
        entity.setDisplayName(employee.getDisplayName());
        entity.setActive(employee.isActive());
        entity.setCompensationType(employee.getCompensationType());
        entity.setFixedAmount(
                employee.getFixedAmount() == null ? null : employee.getFixedAmount().getValue()
        );
        entity.setFrequency(employee.getFrequency());
        entity.setEffectiveFrom(employee.getEffectiveFrom());
        entity.setEffectiveTo(employee.getEffectiveTo());
        return entity;
    }

    public PayrollEmployee toDomain(PayrollEmployeeEntity entity) {
        Objects.requireNonNull(entity, "Payroll employee entity must not be null");

        return PayrollEmployee.reconstitute(
                entity.getId(),
                entity.getDisplayName(),
                entity.isActive(),
                entity.getCompensationType(),
                entity.getFixedAmount() == null ? null : FinancialAmount.of(entity.getFixedAmount()),
                entity.getFrequency(),
                entity.getEffectiveFrom(),
                entity.getEffectiveTo()
        );
    }
}
