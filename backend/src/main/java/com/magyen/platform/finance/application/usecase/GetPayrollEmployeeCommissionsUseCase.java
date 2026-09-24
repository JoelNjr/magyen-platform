package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.GetPayrollEmployeeCommissionsQuery;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeeCommissionsResult;
import com.magyen.platform.finance.application.port.EmployeeSellerCommissionsPort;
import com.magyen.platform.finance.application.port.EmployeeSellerCommissionsPort.EmployeeSellerCommissionsSnapshot;
import com.magyen.platform.finance.domain.PayrollEmployee;
import com.magyen.platform.finance.domain.PayrollEmployeeRepository;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Comisión mensual 5 % de un empleado. No crea asientos ni paga la comisión.
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
        requireCalendarMonth(query.fromDate(), query.toDate());

        PayrollEmployee employee = load(query.employeeId());
        EmployeeSellerCommissionsSnapshot snapshot = employeeSellerCommissionsPort.findCommissions(
                employee.getId(),
                query.fromDate(),
                query.toDate()
        );
        return toResult(employee, snapshot);
    }

    public GetPayrollEmployeeCommissionsResult executeUnbounded(GetPayrollEmployeeCommissionsQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        Objects.requireNonNull(query.employeeId(), "Employee id must not be null");
        PayrollEmployee employee = load(query.employeeId());
        return toResult(employee, employeeSellerCommissionsPort.findUnboundedCommissions(employee.getId()));
    }

    public static void requireCalendarMonth(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            throw new FinanceDomainException("A commission month requires fromDate and toDate");
        }
        LocalDate monthStart = fromDate.withDayOfMonth(1);
        LocalDate monthEnd = fromDate.withDayOfMonth(fromDate.lengthOfMonth());
        if (!fromDate.equals(monthStart) || !toDate.equals(monthEnd)) {
            throw new FinanceDomainException("Commission period must be exactly one calendar month");
        }
    }

    private PayrollEmployee load(java.util.UUID employeeId) {
        return payrollEmployeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Payroll employee not found: " + employeeId));
    }

    private static GetPayrollEmployeeCommissionsResult toResult(
            PayrollEmployee employee,
            EmployeeSellerCommissionsSnapshot snapshot
    ) {
        boolean applicable = employee.isSalesParticipant() || snapshot.numberOfEligibleOrders() > 0;
        return new GetPayrollEmployeeCommissionsResult(
                employee.getId(),
                employee.getDisplayName(),
                employee.getCompensationType(),
                applicable,
                employee.isActive(),
                employee.isEligibleAsSeller(),
                snapshot.fromDate(),
                snapshot.toDate(),
                snapshot.numberOfEligibleOrders(),
                snapshot.totalSales(),
                snapshot.commissionRate() == null ? COMMISSION_RATE_PERCENTAGE : snapshot.commissionRate(),
                snapshot.accumulatedCommission() == null ? ZERO_MONEY : snapshot.accumulatedCommission(),
                snapshot.settlementStatus(),
                snapshot.orders() == null ? List.of() : snapshot.orders()
        );
    }
}
