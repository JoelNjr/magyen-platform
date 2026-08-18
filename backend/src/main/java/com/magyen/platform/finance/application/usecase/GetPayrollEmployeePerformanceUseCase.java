package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.GetPayrollEmployeeCommissionsQuery;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeeCommissionsResult;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeePerformanceQuery;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeePerformanceResult;
import com.magyen.platform.finance.domain.PayrollCompensationType;
import com.magyen.platform.finance.domain.PayrollEmployee;
import com.magyen.platform.finance.domain.PayrollEmployeeRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Lista el desempeño analítico de todos los empleados FIXED_PAYROLL, incluidos inactivos.
 */
public class GetPayrollEmployeePerformanceUseCase {

    private final PayrollEmployeeRepository payrollEmployeeRepository;
    private final GetPayrollEmployeeCommissionsUseCase getPayrollEmployeeCommissionsUseCase;

    public GetPayrollEmployeePerformanceUseCase(
            PayrollEmployeeRepository payrollEmployeeRepository,
            GetPayrollEmployeeCommissionsUseCase getPayrollEmployeeCommissionsUseCase
    ) {
        this.payrollEmployeeRepository = Objects.requireNonNull(
                payrollEmployeeRepository,
                "Payroll employee repository must not be null"
        );
        this.getPayrollEmployeeCommissionsUseCase = Objects.requireNonNull(
                getPayrollEmployeeCommissionsUseCase,
                "Get payroll employee commissions use case must not be null"
        );
    }

    public GetPayrollEmployeePerformanceResult execute(GetPayrollEmployeePerformanceQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        GetPayrollEmployeeCommissionsUseCase.validateRange(query.fromDate(), query.toDate());

        List<GetPayrollEmployeeCommissionsResult> sellers = payrollEmployeeRepository.findAll().stream()
                .filter(employee -> employee.getCompensationType() == PayrollCompensationType.FIXED_PAYROLL)
                .sorted(Comparator.comparing(PayrollEmployee::getDisplayName)
                        .thenComparing(employee -> employee.getId().toString()))
                .map(employee -> getPayrollEmployeeCommissionsUseCase.execute(
                        new GetPayrollEmployeeCommissionsQuery(
                                employee.getId(),
                                query.fromDate(),
                                query.toDate()
                        )
                ))
                .toList();

        return new GetPayrollEmployeePerformanceResult(List.copyOf(sellers));
    }
}
