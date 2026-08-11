package com.magyen.platform.finance.infrastructure.persistence.mapper;

import com.magyen.platform.finance.domain.FinancialAmount;
import com.magyen.platform.finance.domain.PayrollPeriod;
import com.magyen.platform.finance.infrastructure.persistence.entity.PayrollPeriodEntity;

import java.util.Objects;

/**
 * Convierte entre el agregado de dominio y su modelo JPA.
 */
public class PayrollPeriodPersistenceMapper {

    public PayrollPeriodEntity toEntity(PayrollPeriod payrollPeriod) {
        Objects.requireNonNull(payrollPeriod, "Payroll period must not be null");

        PayrollPeriodEntity entity = new PayrollPeriodEntity();
        entity.setId(payrollPeriod.getId());
        entity.setEmployeeId(payrollPeriod.getEmployeeId());
        entity.setPeriodStart(payrollPeriod.getPeriodStart());
        entity.setPeriodEnd(payrollPeriod.getPeriodEnd());
        entity.setExpectedPaymentDate(payrollPeriod.getExpectedPaymentDate());
        entity.setAmountSnapshot(payrollPeriod.getAmountSnapshot().getValue());
        entity.setStatus(payrollPeriod.getStatus());
        entity.setActualPaymentDate(payrollPeriod.getActualPaymentDate());
        entity.setPaidAt(payrollPeriod.getPaidAt());
        entity.setFinancialTransactionId(payrollPeriod.getFinancialTransactionId());
        return entity;
    }

    public PayrollPeriod toDomain(PayrollPeriodEntity entity) {
        Objects.requireNonNull(entity, "Payroll period entity must not be null");

        return PayrollPeriod.reconstitute(
                entity.getId(),
                entity.getEmployeeId(),
                entity.getPeriodStart(),
                entity.getPeriodEnd(),
                entity.getExpectedPaymentDate(),
                FinancialAmount.of(entity.getAmountSnapshot()),
                entity.getStatus(),
                entity.getActualPaymentDate(),
                entity.getPaidAt(),
                entity.getFinancialTransactionId()
        );
    }
}
