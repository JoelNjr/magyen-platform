package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.UpdatePayrollEmployeeCompensationCommand;
import com.magyen.platform.finance.application.dto.UpdatePayrollEmployeeCompensationResult;
import com.magyen.platform.finance.domain.FinancialAmount;
import com.magyen.platform.finance.domain.PayrollCompensationType;
import com.magyen.platform.finance.domain.PayrollEmployee;
import com.magyen.platform.finance.domain.PayrollEmployeeRepository;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;

import java.util.Objects;

/**
 * Actualiza el nombre y, para FIXED_PAYROLL, la compensación fija.
 * <p>
 * No muta períodos ya generados ni crea movimientos del ledger.
 */
public class UpdatePayrollEmployeeCompensationUseCase {

    private final PayrollEmployeeRepository payrollEmployeeRepository;

    public UpdatePayrollEmployeeCompensationUseCase(PayrollEmployeeRepository payrollEmployeeRepository) {
        this.payrollEmployeeRepository = Objects.requireNonNull(
                payrollEmployeeRepository,
                "Payroll employee repository must not be null"
        );
    }

    public UpdatePayrollEmployeeCompensationResult execute(UpdatePayrollEmployeeCompensationCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.employeeId(), "Employee id must not be null");

        if (command.displayName() == null || command.displayName().isBlank()) {
            throw new FinanceDomainException("Display name must not be blank");
        }

        PayrollEmployee employee = payrollEmployeeRepository
                .findById(command.employeeId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payroll employee not found: " + command.employeeId()
                ));

        employee.rename(command.displayName());

        if (employee.getCompensationType() == PayrollCompensationType.FIXED_PAYROLL) {
            if (command.fixedAmount() == null) {
                throw new FinanceDomainException("Fixed amount must not be null");
            }
            if (command.effectiveFrom() == null) {
                throw new FinanceDomainException("Effective from must not be null");
            }
            employee.updateFixedCompensation(
                    FinancialAmount.of(command.fixedAmount()),
                    command.effectiveFrom(),
                    command.effectiveTo()
            );
        }

        PayrollEmployee saved = payrollEmployeeRepository.save(employee);
        return PayrollEmployeeReadMapper.toUpdateResult(saved);
    }
}
