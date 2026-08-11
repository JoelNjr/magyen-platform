package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.DeactivatePayrollEmployeeCommand;
import com.magyen.platform.finance.application.dto.DeactivatePayrollEmployeeResult;
import com.magyen.platform.finance.domain.PayrollEmployee;
import com.magyen.platform.finance.domain.PayrollEmployeeRepository;

import java.util.Objects;

/**
 * Desactiva un empleado de nómina.
 * <p>
 * El empleado permanece legible históricamente. No elimina períodos ni crea ledger.
 */
public class DeactivatePayrollEmployeeUseCase {

    private final PayrollEmployeeRepository payrollEmployeeRepository;

    public DeactivatePayrollEmployeeUseCase(PayrollEmployeeRepository payrollEmployeeRepository) {
        this.payrollEmployeeRepository = Objects.requireNonNull(
                payrollEmployeeRepository,
                "Payroll employee repository must not be null"
        );
    }

    public DeactivatePayrollEmployeeResult execute(DeactivatePayrollEmployeeCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.employeeId(), "Employee id must not be null");

        PayrollEmployee employee = payrollEmployeeRepository
                .findById(command.employeeId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payroll employee not found: " + command.employeeId()
                ));

        employee.deactivate();
        PayrollEmployee saved = payrollEmployeeRepository.save(employee);

        return new DeactivatePayrollEmployeeResult(saved.getId(), saved.isActive());
    }
}
