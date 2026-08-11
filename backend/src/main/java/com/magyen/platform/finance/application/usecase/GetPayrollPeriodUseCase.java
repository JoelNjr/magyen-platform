package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.GetPayrollPeriodQuery;
import com.magyen.platform.finance.application.dto.GetPayrollPeriodResult;
import com.magyen.platform.finance.domain.PayrollEmployee;
import com.magyen.platform.finance.domain.PayrollEmployeeRepository;
import com.magyen.platform.finance.domain.PayrollPeriod;
import com.magyen.platform.finance.domain.PayrollPeriodRepository;

import java.util.Objects;

/**
 * Consulta un período de nómina por identidad, enriquecido con el nombre del empleado.
 */
public class GetPayrollPeriodUseCase {

    private final PayrollPeriodRepository payrollPeriodRepository;
    private final PayrollEmployeeRepository payrollEmployeeRepository;

    public GetPayrollPeriodUseCase(
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

    public GetPayrollPeriodResult execute(GetPayrollPeriodQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        Objects.requireNonNull(query.periodId(), "Period id must not be null");

        PayrollPeriod period = payrollPeriodRepository
                .findById(query.periodId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payroll period not found: " + query.periodId()
                ));

        String displayName = payrollEmployeeRepository
                .findById(period.getEmployeeId())
                .map(PayrollEmployee::getDisplayName)
                .orElse("Unknown employee");

        return PayrollPeriodReadMapper.toGetResult(period, displayName);
    }
}
