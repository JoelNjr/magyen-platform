package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.GetPayrollEmployeeQuery;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeeResult;
import com.magyen.platform.finance.domain.PayrollEmployee;
import com.magyen.platform.finance.domain.PayrollEmployeeRepository;

import java.util.Objects;

/**
 * Consulta un empleado de nómina por identidad.
 */
public class GetPayrollEmployeeUseCase {

    private final PayrollEmployeeRepository payrollEmployeeRepository;

    public GetPayrollEmployeeUseCase(PayrollEmployeeRepository payrollEmployeeRepository) {
        this.payrollEmployeeRepository = Objects.requireNonNull(
                payrollEmployeeRepository,
                "Payroll employee repository must not be null"
        );
    }

    public GetPayrollEmployeeResult execute(GetPayrollEmployeeQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        Objects.requireNonNull(query.employeeId(), "Employee id must not be null");

        PayrollEmployee employee = payrollEmployeeRepository
                .findById(query.employeeId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payroll employee not found: " + query.employeeId()
                ));

        return PayrollEmployeeReadMapper.toGetResult(employee);
    }
}
