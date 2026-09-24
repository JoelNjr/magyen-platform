package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.GetPayrollEmployeeCommissionsResult;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeePerformanceQuery;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeePerformanceResult;
import com.magyen.platform.finance.application.port.EmployeeSellerCommissionsPort;
import com.magyen.platform.finance.application.port.EmployeeSellerCommissionsPort.EmployeeSellerCommissionsSnapshot;
import com.magyen.platform.finance.domain.PayrollEmployee;
import com.magyen.platform.finance.domain.PayrollEmployeeRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Lista la comisión mensual de quienes tienen pedidos confirmados en ese mes.
 * <p>
 * No incluye empleados de sueldo fijo sin actividad. No crea asientos.
 */
public class GetPayrollEmployeePerformanceUseCase {

    private final PayrollEmployeeRepository payrollEmployeeRepository;
    private final EmployeeSellerCommissionsPort employeeSellerCommissionsPort;

    public GetPayrollEmployeePerformanceUseCase(
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

    public GetPayrollEmployeePerformanceResult execute(GetPayrollEmployeePerformanceQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        GetPayrollEmployeeCommissionsUseCase.requireCalendarMonth(query.fromDate(), query.toDate());

        List<GetPayrollEmployeeCommissionsResult> sellers = new ArrayList<>();
        for (EmployeeSellerCommissionsSnapshot snapshot : employeeSellerCommissionsPort.findCommissionsForMonth(
                query.fromDate(),
                query.toDate()
        )) {
            payrollEmployeeRepository.findById(snapshot.sellerEmployeeId()).ifPresent(employee ->
                    sellers.add(toResult(employee, snapshot))
            );
        }
        sellers.sort(Comparator.comparing(GetPayrollEmployeeCommissionsResult::displayName)
                .thenComparing(result -> result.employeeId().toString()));
        return new GetPayrollEmployeePerformanceResult(List.copyOf(sellers));
    }

    private static GetPayrollEmployeeCommissionsResult toResult(
            PayrollEmployee employee,
            EmployeeSellerCommissionsSnapshot snapshot
    ) {
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
                snapshot.accumulatedCommission(),
                snapshot.settlementStatus(),
                snapshot.orders()
        );
    }
}
