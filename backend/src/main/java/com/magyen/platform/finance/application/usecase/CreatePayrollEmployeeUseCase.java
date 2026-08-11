package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.CreatePayrollEmployeeCommand;
import com.magyen.platform.finance.application.dto.CreatePayrollEmployeeResult;
import com.magyen.platform.finance.domain.FinancialAmount;
import com.magyen.platform.finance.domain.PayrollCompensationType;
import com.magyen.platform.finance.domain.PayrollEmployee;
import com.magyen.platform.finance.domain.PayrollEmployeeRepository;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;

import java.util.Objects;

/**
 * Caso de uso que registra un empleado de nómina.
 * <p>
 * No crea {@code FinancialTransaction} ni períodos. Solo persiste el empleado.
 */
public class CreatePayrollEmployeeUseCase {

    private final PayrollEmployeeRepository payrollEmployeeRepository;

    public CreatePayrollEmployeeUseCase(PayrollEmployeeRepository payrollEmployeeRepository) {
        this.payrollEmployeeRepository = Objects.requireNonNull(
                payrollEmployeeRepository,
                "Payroll employee repository must not be null"
        );
    }

    public CreatePayrollEmployeeResult execute(CreatePayrollEmployeeCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        PayrollEmployee employee;
        if (command.compensationType() == PayrollCompensationType.FIXED_PAYROLL) {
            employee = PayrollEmployee.createFixed(
                    command.displayName(),
                    FinancialAmount.of(command.fixedAmount()),
                    command.effectiveFrom(),
                    command.effectiveTo()
            );
        } else if (command.compensationType() == PayrollCompensationType.PRODUCTION_BASED) {
            employee = PayrollEmployee.createProductionBased(command.displayName());
        } else {
            throw new FinanceDomainException(
                    "Unsupported payroll compensation type: " + command.compensationType()
            );
        }

        PayrollEmployee saved = payrollEmployeeRepository.save(employee);
        return PayrollEmployeeReadMapper.toCreateResult(saved);
    }

    private void validateCommand(CreatePayrollEmployeeCommand command) {
        if (command.displayName() == null || command.displayName().isBlank()) {
            throw new FinanceDomainException("Display name must not be blank");
        }
        if (command.compensationType() == null) {
            throw new FinanceDomainException("Compensation type must not be null");
        }
        if (command.compensationType() == PayrollCompensationType.FIXED_PAYROLL) {
            if (command.fixedAmount() == null) {
                throw new FinanceDomainException("Fixed amount must not be null");
            }
            if (command.effectiveFrom() == null) {
                throw new FinanceDomainException("Effective from must not be null");
            }
        }
    }
}
