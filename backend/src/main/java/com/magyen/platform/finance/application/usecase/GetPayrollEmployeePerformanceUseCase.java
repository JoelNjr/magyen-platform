package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.GetPayrollEmployeeCommissionsResult;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeePerformanceQuery;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeePerformanceResult;
import com.magyen.platform.finance.application.port.EmployeeSellerCommissionsPort;
import com.magyen.platform.finance.application.port.EmployeeSellerCommissionsPort.EmployeeSellerCommissionsSnapshot;
import com.magyen.platform.finance.domain.PayrollEmployeeRepository;
import com.magyen.platform.finance.domain.SellerCommissionSettlement;
import com.magyen.platform.finance.domain.SellerCommissionSettlementRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Lista la comisión mensual de quienes tienen pedidos confirmados en ese mes.
 * <p>
 * No incluye empleados de sueldo fijo sin actividad. No crea asientos.
 */
public class GetPayrollEmployeePerformanceUseCase {

    private final PayrollEmployeeRepository payrollEmployeeRepository;
    private final EmployeeSellerCommissionsPort employeeSellerCommissionsPort;
    private final SellerCommissionSettlementRepository sellerCommissionSettlementRepository;

    public GetPayrollEmployeePerformanceUseCase(
            PayrollEmployeeRepository payrollEmployeeRepository,
            EmployeeSellerCommissionsPort employeeSellerCommissionsPort,
            SellerCommissionSettlementRepository sellerCommissionSettlementRepository
    ) {
        this.payrollEmployeeRepository = Objects.requireNonNull(
                payrollEmployeeRepository,
                "Payroll employee repository must not be null"
        );
        this.employeeSellerCommissionsPort = Objects.requireNonNull(
                employeeSellerCommissionsPort,
                "Employee seller commissions port must not be null"
        );
        this.sellerCommissionSettlementRepository = Objects.requireNonNull(
                sellerCommissionSettlementRepository,
                "Seller commission settlement repository must not be null"
        );
    }

    public GetPayrollEmployeePerformanceResult execute(GetPayrollEmployeePerformanceQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        GetPayrollEmployeeCommissionsUseCase.requireCalendarMonth(query.fromDate(), query.toDate());

        List<GetPayrollEmployeeCommissionsResult> sellers = new ArrayList<>();
        Set<UUID> listedEmployeeIds = new HashSet<>();
        for (EmployeeSellerCommissionsSnapshot snapshot : employeeSellerCommissionsPort.findCommissionsForMonth(
                query.fromDate(),
                query.toDate()
        )) {
            payrollEmployeeRepository.findById(snapshot.sellerEmployeeId()).ifPresent(employee -> {
                SellerCommissionSettlement settlement = sellerCommissionSettlementRepository
                        .findByEmployeeIdAndPeriodStart(employee.getId(), query.fromDate())
                        .orElse(null);
                sellers.add(GetPayrollEmployeeCommissionsUseCase.toResult(employee, snapshot, settlement));
                listedEmployeeIds.add(employee.getId());
            });
        }
        for (SellerCommissionSettlement settlement : sellerCommissionSettlementRepository.findByPeriodStart(
                query.fromDate()
        )) {
            if (listedEmployeeIds.contains(settlement.getEmployeeId())) {
                continue;
            }
            payrollEmployeeRepository.findById(settlement.getEmployeeId()).ifPresent(employee -> {
                EmployeeSellerCommissionsSnapshot snapshot = employeeSellerCommissionsPort.findCommissions(
                        employee.getId(),
                        query.fromDate(),
                        query.toDate()
                );
                sellers.add(GetPayrollEmployeeCommissionsUseCase.toResult(employee, snapshot, settlement));
            });
        }
        sellers.sort(Comparator.comparing(GetPayrollEmployeeCommissionsResult::displayName)
                .thenComparing(result -> result.employeeId().toString()));
        return new GetPayrollEmployeePerformanceResult(List.copyOf(sellers));
    }
}
