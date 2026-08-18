package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.GetPayrollEmployeeCommissionsQuery;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeeCommissionsResult;
import com.magyen.platform.finance.application.port.EmployeeSellerCommissionsPort;
import com.magyen.platform.finance.application.port.EmployeeSellerCommissionsPort.EmployeeSellerCommissionsSnapshot;
import com.magyen.platform.finance.domain.PayrollCompensationType;
import com.magyen.platform.finance.domain.PayrollEmployee;
import com.magyen.platform.finance.domain.PayrollEmployeeRepository;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Comisión analítica 5 % de un empleado FIXED_PAYROLL. No crea asientos ni paga nómina.
 */
public class GetPayrollEmployeeCommissionsUseCase {

    private static final BigDecimal ZERO_MONEY = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal COMMISSION_RATE_PERCENTAGE = new BigDecimal("5.00");

    private final PayrollEmployeeRepository payrollEmployeeRepository;
    private final EmployeeSellerCommissionsPort employeeSellerCommissionsPort;

    public GetPayrollEmployeeCommissionsUseCase(
            PayrollEmployeeRepository payrollEmployeeRepository,
            EmployeeSellerCommissionsPort employeeSellerCommissionsPort
    ) {
        this.payrollEmployeeRepository = Objects.requireNonNull(
                payrollEmployeeRepository,
                "Payroll employee repository must not be null"
        );
        this.employeeSellerCommissionsPort = Objects.requireNonNull(
                employeeSellerCommissionsPort,
                "Employee seller commissions port must not be null"
        );
    }

    public GetPayrollEmployeeCommissionsResult execute(GetPayrollEmployeeCommissionsQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        Objects.requireNonNull(query.employeeId(), "Employee id must not be null");
        validateRange(query.fromDate(), query.toDate());

        PayrollEmployee employee = payrollEmployeeRepository.findById(query.employeeId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payroll employee not found: " + query.employeeId()
                ));

        if (employee.getCompensationType() != PayrollCompensationType.FIXED_PAYROLL) {
            return empty(employee, query);
        }

        EmployeeSellerCommissionsSnapshot snapshot = employeeSellerCommissionsPort.findCommissions(
                employee.getId(),
                query.fromDate(),
                query.toDate()
        );

        return new GetPayrollEmployeeCommissionsResult(
                employee.getId(),
                employee.getDisplayName(),
                employee.getCompensationType(),
                true,
                employee.isActive(),
                employee.isEligibleAsSeller(),
                snapshot.fromDate(),
                snapshot.toDate(),
                snapshot.numberOfEligibleOrders(),
                snapshot.totalSales(),
                snapshot.commissionRate(),
                snapshot.accumulatedCommission()
        );
    }

    private static GetPayrollEmployeeCommissionsResult empty(
            PayrollEmployee employee,
            GetPayrollEmployeeCommissionsQuery query
    ) {
        return new GetPayrollEmployeeCommissionsResult(
                employee.getId(),
                employee.getDisplayName(),
                employee.getCompensationType(),
                false,
                employee.isActive(),
                false,
                query.fromDate(),
                query.toDate(),
                0,
                ZERO_MONEY,
                COMMISSION_RATE_PERCENTAGE,
                ZERO_MONEY
        );
    }

    static void validateRange(java.time.LocalDate fromDate, java.time.LocalDate toDate) {
        if (fromDate == null && toDate == null) {
            return;
        }
        if (fromDate == null || toDate == null) {
            throw new FinanceDomainException("Both fromDate and toDate must be provided together");
        }
        if (fromDate.isAfter(toDate)) {
            throw new FinanceDomainException("From date must not be after to date");
        }
    }
}
