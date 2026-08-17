package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.CreatePayrollDeductionCommand;
import com.magyen.platform.finance.application.dto.CreatePayrollDeductionResult;
import com.magyen.platform.finance.domain.FinancialAmount;
import com.magyen.platform.finance.domain.PayrollDeduction;
import com.magyen.platform.finance.domain.PayrollDeductionRepository;
import com.magyen.platform.finance.domain.PayrollEmployeeRepository;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Registra un descuento de nómina contra un empleado existente.
 * <p>
 * No crea {@code FinancialTransaction}. El descuento no es un gasto de Magyen.
 */
public class CreatePayrollDeductionUseCase {

    private final PayrollEmployeeRepository payrollEmployeeRepository;
    private final PayrollDeductionRepository payrollDeductionRepository;
    private final Clock clock;

    public CreatePayrollDeductionUseCase(
            PayrollEmployeeRepository payrollEmployeeRepository,
            PayrollDeductionRepository payrollDeductionRepository,
            Clock clock
    ) {
        this.payrollEmployeeRepository = Objects.requireNonNull(
                payrollEmployeeRepository,
                "Payroll employee repository must not be null"
        );
        this.payrollDeductionRepository = Objects.requireNonNull(
                payrollDeductionRepository,
                "Payroll deduction repository must not be null"
        );
        this.clock = Objects.requireNonNull(clock, "Clock must not be null");
    }

    @Transactional
    public CreatePayrollDeductionResult execute(CreatePayrollDeductionCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        payrollEmployeeRepository.findById(command.employeeId())
                .orElseThrow(() -> new FinanceDomainException(
                        "Payroll employee not found: " + command.employeeId()
                ));

        PayrollDeduction deduction = PayrollDeduction.create(
                command.employeeId(),
                command.type(),
                FinancialAmount.of(command.amount()),
                command.deductionDate(),
                command.description(),
                LocalDateTime.now(clock)
        );

        PayrollDeduction saved = payrollDeductionRepository.save(deduction);
        return PayrollDeductionReadMapper.toCreateResult(saved);
    }

    private void validateCommand(CreatePayrollDeductionCommand command) {
        if (command.employeeId() == null) {
            throw new FinanceDomainException("Employee id must not be null");
        }
        if (command.type() == null) {
            throw new FinanceDomainException("Payroll deduction type must not be null");
        }
        if (command.amount() == null) {
            throw new FinanceDomainException("Payroll deduction amount must not be null");
        }
        if (command.deductionDate() == null) {
            throw new FinanceDomainException("Deduction date must not be null");
        }
    }
}
