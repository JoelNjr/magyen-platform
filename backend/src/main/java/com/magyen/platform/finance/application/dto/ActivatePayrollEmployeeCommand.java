package com.magyen.platform.finance.application.dto;

import java.util.UUID;

/**
 * Entrada del caso de uso para activar un empleado de nómina.
 */
public record ActivatePayrollEmployeeCommand(
        UUID employeeId
) {
}
