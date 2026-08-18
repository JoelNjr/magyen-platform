package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.GetPayrollDeductionsQuery;
import com.magyen.platform.finance.application.dto.GetPayrollDeductionsResult;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeeCommissionsQuery;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeeCommissionsResult;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeeFinancialSummaryQuery;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeeFinancialSummaryResult;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeeProductionEarningsQuery;
import com.magyen.platform.finance.application.dto.GetPayrollEmployeeProductionEarningsResult;
import com.magyen.platform.finance.domain.PayrollCompensationType;
import com.magyen.platform.finance.domain.PayrollDeductionStatus;
import com.magyen.platform.finance.domain.PayrollEmployee;
import com.magyen.platform.finance.domain.PayrollEmployeeRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Resumen de compensación V1. Compone comisión, mano de obra y descuentos ACTIVE.
 * <p>
 * No liquida nómina ni calcula un salario neto.
 */
public class GetPayrollEmployeeFinancialSummaryUseCase {

    private static final LocalDate HISTORICAL_FROM = LocalDate.of(1970, 1, 1);
    private static final BigDecimal ZERO_MONEY = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final PayrollEmployeeRepository payrollEmployeeRepository;
    private final GetPayrollEmployeeCommissionsUseCase getPayrollEmployeeCommissionsUseCase;
    private final GetPayrollEmployeeProductionEarningsUseCase getPayrollEmployeeProductionEarningsUseCase;
    private final GetPayrollDeductionsUseCase getPayrollDeductionsUseCase;
    private final Clock clock;

    public GetPayrollEmployeeFinancialSummaryUseCase(
            PayrollEmployeeRepository payrollEmployeeRepository,
            GetPayrollEmployeeCommissionsUseCase getPayrollEmployeeCommissionsUseCase,
            GetPayrollEmployeeProductionEarningsUseCase getPayrollEmployeeProductionEarningsUseCase,
            GetPayrollDeductionsUseCase getPayrollDeductionsUseCase,
            Clock clock
    ) {
        this.payrollEmployeeRepository = Objects.requireNonNull(
                payrollEmployeeRepository,
                "Payroll employee repository must not be null"
        );
        this.getPayrollEmployeeCommissionsUseCase = Objects.requireNonNull(
                getPayrollEmployeeCommissionsUseCase,
                "Get payroll employee commissions use case must not be null"
        );
        this.getPayrollEmployeeProductionEarningsUseCase = Objects.requireNonNull(
                getPayrollEmployeeProductionEarningsUseCase,
                "Get payroll employee production earnings use case must not be null"
        );
        this.getPayrollDeductionsUseCase = Objects.requireNonNull(
                getPayrollDeductionsUseCase,
                "Get payroll deductions use case must not be null"
        );
        this.clock = Objects.requireNonNull(clock, "Clock must not be null");
    }

    public GetPayrollEmployeeFinancialSummaryResult execute(GetPayrollEmployeeFinancialSummaryQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        Objects.requireNonNull(query.employeeId(), "Employee id must not be null");
        GetPayrollEmployeeCommissionsUseCase.validateRange(query.fromDate(), query.toDate());

        PayrollEmployee employee = payrollEmployeeRepository.findById(query.employeeId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payroll employee not found: " + query.employeeId()
                ));

        GetPayrollEmployeeCommissionsResult commissions = getPayrollEmployeeCommissionsUseCase.execute(
                new GetPayrollEmployeeCommissionsQuery(query.employeeId(), query.fromDate(), query.toDate())
        );

        LocalDate productionFrom = query.fromDate() == null ? HISTORICAL_FROM : query.fromDate();
        LocalDate productionTo = query.toDate() == null ? LocalDate.now(clock) : query.toDate();
        GetPayrollEmployeeProductionEarningsResult production = getPayrollEmployeeProductionEarningsUseCase.execute(
                new GetPayrollEmployeeProductionEarningsQuery(query.employeeId(), productionFrom, productionTo)
        );

        GetPayrollDeductionsResult deductions = getPayrollDeductionsUseCase.execute(
                new GetPayrollDeductionsQuery(query.employeeId(), PayrollDeductionStatus.ACTIVE)
        );

        BigDecimal fixedAmount = employee.getCompensationType() == PayrollCompensationType.FIXED_PAYROLL
                && employee.getFixedAmount() != null
                ? employee.getFixedAmount().getValue()
                : null;

        return new GetPayrollEmployeeFinancialSummaryResult(
                employee.getId(),
                employee.getDisplayName(),
                employee.isActive(),
                employee.getCompensationType(),
                fixedAmount,
                commissions.sellerCommissionApplicable(),
                production.productionLaborApplicable(),
                commissions.eligibleForNewQuotations(),
                query.fromDate(),
                query.toDate(),
                commissions.numberOfEligibleOrders(),
                commissions.totalSales(),
                commissions.commissionRate(),
                commissions.accumulatedCommission(),
                production.laborWorkCount(),
                production.totalCalculatedAmount(),
                production.totalPaidAmount(),
                production.totalPendingAmount(),
                deductions.activeCount(),
                deductions.activeTotal() == null ? ZERO_MONEY : deductions.activeTotal()
        );
    }
}
