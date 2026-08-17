package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.GetPayrollEmployeeProductionEarningsQuery;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeeProductionEarningsResult;
import com.magyen.platform.finance.application.port.EmployeeProductionEarningsPort;
import com.magyen.platform.finance.application.port.EmployeeProductionEarningsPort.EmployeeProductionEarningsSnapshot;
import com.magyen.platform.finance.domain.PayrollCompensationType;
import com.magyen.platform.finance.domain.PayrollEmployee;
import com.magyen.platform.finance.domain.PayrollEmployeeRepository;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Resume la mano de obra de un empleado PRODUCTION_BASED. No es un motor de nómina.
 */
public class GetPayrollEmployeeProductionEarningsUseCase {

    private static final BigDecimal ZERO_MONEY = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal ZERO_QUANTITY = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);

    private final PayrollEmployeeRepository payrollEmployeeRepository;
    private final EmployeeProductionEarningsPort employeeProductionEarningsPort;

    public GetPayrollEmployeeProductionEarningsUseCase(
            PayrollEmployeeRepository payrollEmployeeRepository,
            EmployeeProductionEarningsPort employeeProductionEarningsPort
    ) {
        this.payrollEmployeeRepository = Objects.requireNonNull(
                payrollEmployeeRepository,
                "Payroll employee repository must not be null"
        );
        this.employeeProductionEarningsPort = Objects.requireNonNull(
                employeeProductionEarningsPort,
                "Employee production earnings port must not be null"
        );
    }

    public GetPayrollEmployeeProductionEarningsResult execute(
            GetPayrollEmployeeProductionEarningsQuery query
    ) {
        Objects.requireNonNull(query, "Query must not be null");
        Objects.requireNonNull(query.employeeId(), "Employee id must not be null");
        Objects.requireNonNull(query.fromDate(), "From date must not be null");
        Objects.requireNonNull(query.toDate(), "To date must not be null");
        if (query.toDate().isBefore(query.fromDate())) {
            throw new FinanceDomainException("Earnings to date must not be before from date");
        }

        PayrollEmployee employee = payrollEmployeeRepository.findById(query.employeeId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payroll employee not found: " + query.employeeId()
                ));

        if (employee.getCompensationType() != PayrollCompensationType.PRODUCTION_BASED) {
            return new GetPayrollEmployeeProductionEarningsResult(
                    employee.getId(),
                    employee.getDisplayName(),
                    employee.getCompensationType(),
                    false,
                    query.fromDate(),
                    query.toDate(),
                    0,
                    ZERO_QUANTITY,
                    ZERO_MONEY,
                    ZERO_MONEY,
                    ZERO_MONEY
            );
        }

        EmployeeProductionEarningsSnapshot snapshot = employeeProductionEarningsPort.findEarnings(
                employee.getId(),
                query.fromDate(),
                query.toDate()
        );

        return new GetPayrollEmployeeProductionEarningsResult(
                employee.getId(),
                employee.getDisplayName(),
                employee.getCompensationType(),
                true,
                snapshot.fromDate(),
                snapshot.toDate(),
                snapshot.laborWorkCount(),
                snapshot.totalQuantity(),
                snapshot.totalCalculatedAmount(),
                snapshot.totalPaidAmount(),
                snapshot.totalPendingAmount()
        );
    }
}
