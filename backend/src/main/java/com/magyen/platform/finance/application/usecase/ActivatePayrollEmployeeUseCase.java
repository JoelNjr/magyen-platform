package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.ActivatePayrollEmployeeCommand;
import com.magyen.platform.finance.application.dto.ActivatePayrollEmployeeResult;
import com.magyen.platform.finance.domain.PayrollEmployee;
import com.magyen.platform.finance.domain.PayrollEmployeeRepository;

import java.util.Objects;

/**
 * Activa un empleado de nómina para que vuelva a participar en generación fija.
 */
public class ActivatePayrollEmployeeUseCase {

    private final PayrollEmployeeRepository payrollEmployeeRepository;

    public ActivatePayrollEmployeeUseCase(PayrollEmployeeRepository payrollEmployeeRepository) {
        this.payrollEmployeeRepository = Objects.requireNonNull(
                payrollEmployeeRepository,
                "Payroll employee repository must not be null"
        );
    }

    public ActivatePayrollEmployeeResult execute(ActivatePayrollEmployeeCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.employeeId(), "Employee id must not be null");

        PayrollEmployee employee = payrollEmployeeRepository
                .findById(command.employeeId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payroll employee not found: " + command.employeeId()
                ));

        employee.activate();
        PayrollEmployee saved = payrollEmployeeRepository.save(employee);

        return new ActivatePayrollEmployeeResult(saved.getId(), saved.isActive());
    }
}
