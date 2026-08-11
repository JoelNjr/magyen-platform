package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.GetPayrollPeriodsQuery;
import com.magyen.platform.finance.application.dto.GetPayrollPeriodsResult;
import com.magyen.platform.finance.domain.PayrollEmployee;
import com.magyen.platform.finance.domain.PayrollEmployeeRepository;
import com.magyen.platform.finance.domain.PayrollPeriod;
import com.magyen.platform.finance.domain.PayrollPeriodRepository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Lista períodos de nómina del más reciente al más antiguo.
 */
public class GetPayrollPeriodsUseCase {

    private final PayrollPeriodRepository payrollPeriodRepository;
    private final PayrollEmployeeRepository payrollEmployeeRepository;

    public GetPayrollPeriodsUseCase(
            PayrollPeriodRepository payrollPeriodRepository,
            PayrollEmployeeRepository payrollEmployeeRepository
    ) {
        this.payrollPeriodRepository = Objects.requireNonNull(
                payrollPeriodRepository,
                "Payroll period repository must not be null"
        );
        this.payrollEmployeeRepository = Objects.requireNonNull(
                payrollEmployeeRepository,
                "Payroll employee repository must not be null"
        );
    }

    public GetPayrollPeriodsResult execute() {
        return execute(GetPayrollPeriodsQuery.all());
    }

    public GetPayrollPeriodsResult execute(GetPayrollPeriodsQuery query) {
        Objects.requireNonNull(query, "Query must not be null");

        Map<UUID, PayrollEmployee> employeesById = payrollEmployeeRepository.findAll().stream()
                .collect(Collectors.toMap(PayrollEmployee::getId, Function.identity()));

        List<PayrollPeriod> periods = payrollPeriodRepository.findAllNewestFirst();

        return new GetPayrollPeriodsResult(
                periods.stream()
                        .map(period -> PayrollPeriodReadMapper.toGetResult(period, employeesById))
                        .toList()
        );
    }
}
