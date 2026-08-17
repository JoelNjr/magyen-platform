package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.CancelPayrollDeductionCommand;
import com.magyen.platform.finance.application.dto.CancelPayrollDeductionResult;
import com.magyen.platform.finance.domain.PayrollDeduction;
import com.magyen.platform.finance.domain.PayrollDeductionRepository;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Cancela un descuento preservando historia. No genera movimientos del ledger.
 */
public class CancelPayrollDeductionUseCase {

    private final PayrollDeductionRepository payrollDeductionRepository;

    public CancelPayrollDeductionUseCase(PayrollDeductionRepository payrollDeductionRepository) {
        this.payrollDeductionRepository = Objects.requireNonNull(
                payrollDeductionRepository,
                "Payroll deduction repository must not be null"
        );
    }

    @Transactional
    public CancelPayrollDeductionResult execute(CancelPayrollDeductionCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.employeeId(), "Employee id must not be null");
        Objects.requireNonNull(command.deductionId(), "Deduction id must not be null");

        PayrollDeduction deduction = payrollDeductionRepository.findById(command.deductionId())
                .orElseThrow(() -> new FinanceDomainException(
                        "Payroll deduction not found: " + command.deductionId()
                ));

        if (!deduction.getEmployeeId().equals(command.employeeId())) {
            throw new FinanceDomainException(
                    "Payroll deduction does not belong to employee: " + command.employeeId()
            );
        }

        deduction.cancel();
        PayrollDeduction saved = payrollDeductionRepository.save(deduction);
        return PayrollDeductionReadMapper.toCancelResult(saved);
    }
}
