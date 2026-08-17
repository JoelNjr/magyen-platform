package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.GetPayrollDeductionsQuery;
import com.magyen.platform.finance.application.dto.GetPayrollDeductionsResult;
import com.magyen.platform.finance.application.dto.PayrollDeductionResult;
import com.magyen.platform.finance.domain.PayrollDeduction;
import com.magyen.platform.finance.domain.PayrollDeductionRepository;
import com.magyen.platform.finance.domain.PayrollEmployee;
import com.magyen.platform.finance.domain.PayrollEmployeeRepository;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * Lista descuentos de un empleado. Los totales incluyen solo descuentos ACTIVE.
 */
public class GetPayrollDeductionsUseCase {

    private static final int MONEY_SCALE = 2;
    private static final RoundingMode MONEY_ROUNDING = RoundingMode.HALF_UP;

    private final PayrollEmployeeRepository payrollEmployeeRepository;
    private final PayrollDeductionRepository payrollDeductionRepository;

    public GetPayrollDeductionsUseCase(
            PayrollEmployeeRepository payrollEmployeeRepository,
            PayrollDeductionRepository payrollDeductionRepository
    ) {
        this.payrollEmployeeRepository = Objects.requireNonNull(
                payrollEmployeeRepository,
                "Payroll employee repository must not be null"
        );
        this.payrollDeductionRepository = Objects.requireNonNull(
                payrollDeductionRepository,
                "Payroll deduction repository must not be null"
        );
    }

    public GetPayrollDeductionsResult execute(GetPayrollDeductionsQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        Objects.requireNonNull(query.employeeId(), "Employee id must not be null");

        PayrollEmployee employee = payrollEmployeeRepository.findById(query.employeeId())
                .orElseThrow(() -> new FinanceDomainException(
                        "Payroll employee not found: " + query.employeeId()
                ));

        List<PayrollDeduction> history = payrollDeductionRepository.findByEmployeeId(query.employeeId());
        List<PayrollDeduction> visible = query.status() == null
                ? history
                : history.stream()
                .filter(deduction -> deduction.getStatus() == query.status())
                .toList();

        List<PayrollDeductionResult> results = visible.stream()
                .map(PayrollDeductionReadMapper::toResult)
                .toList();

        List<PayrollDeduction> active = history.stream()
                .filter(PayrollDeduction::isActive)
                .toList();
        BigDecimal activeTotal = active.stream()
                .map(deduction -> deduction.getAmount().getValue())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(MONEY_SCALE, MONEY_ROUNDING);

        return new GetPayrollDeductionsResult(
                employee.getId(),
                employee.getDisplayName(),
                results,
                active.size(),
                activeTotal
        );
    }
}
