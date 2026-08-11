package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.ResolveProductionLaborOperatorQuery;
import com.magyen.platform.finance.application.dto.ResolveProductionLaborOperatorResult;
import com.magyen.platform.finance.domain.PayrollEmployee;
import com.magyen.platform.finance.domain.PayrollEmployeeRepository;

import java.util.Objects;

/**
 * Resuelve un empleado de nómina para el flujo de mano de obra de Production.
 */
public class ResolveProductionLaborOperatorUseCase {

    private final PayrollEmployeeRepository payrollEmployeeRepository;

    public ResolveProductionLaborOperatorUseCase(PayrollEmployeeRepository payrollEmployeeRepository) {
        this.payrollEmployeeRepository = Objects.requireNonNull(
                payrollEmployeeRepository,
                "Payroll employee repository must not be null"
        );
    }

    public ResolveProductionLaborOperatorResult execute(ResolveProductionLaborOperatorQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        Objects.requireNonNull(query.employeeId(), "Employee id must not be null");

        PayrollEmployee employee = payrollEmployeeRepository
                .findById(query.employeeId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payroll employee not found: " + query.employeeId()
                ));

        return new ResolveProductionLaborOperatorResult(
                employee.getId(),
                employee.getDisplayName(),
                employee.isActive(),
                employee.getCompensationType()
        );
    }
}
