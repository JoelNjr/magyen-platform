package com.magyen.platform.finance.application.dto;

import java.util.UUID;

/**
 * Entrada del caso de uso para desactivar un empleado de nómina.
 */
public record DeactivatePayrollEmployeeCommand(
        UUID employeeId
) {
}
