package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.GetPayrollEmployeesQuery;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeesResult;
import com.magyen.platform.finance.domain.PayrollEmployee;
import com.magyen.platform.finance.domain.PayrollEmployeeRepository;

import java.util.List;
import java.util.Objects;

/**
 * Caso de uso que lista empleados de nómina.
 */
public class GetPayrollEmployeesUseCase {

    private final PayrollEmployeeRepository payrollEmployeeRepository;

    public GetPayrollEmployeesUseCase(PayrollEmployeeRepository payrollEmployeeRepository) {
        this.payrollEmployeeRepository = Objects.requireNonNull(
                payrollEmployeeRepository,
                "Payroll employee repository must not be null"
        );
    }

    public GetPayrollEmployeesResult execute() {
        return execute(GetPayrollEmployeesQuery.all());
    }

    public GetPayrollEmployeesResult execute(GetPayrollEmployeesQuery query) {
        Objects.requireNonNull(query, "Query must not be null");

        List<PayrollEmployee> employees = query.active() == null
                ? payrollEmployeeRepository.findAll()
                : payrollEmployeeRepository.findByActive(query.active());

        return new GetPayrollEmployeesResult(
                employees.stream()
                        .map(PayrollEmployeeReadMapper::toGetResult)
                        .toList()
        );
    }
}
