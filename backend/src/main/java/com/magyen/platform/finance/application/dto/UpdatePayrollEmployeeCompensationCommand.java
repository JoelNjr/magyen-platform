package com.magyen.platform.finance.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entrada para renombrar y/o actualizar la compensación fija de un empleado.
 * <p>
 * Para {@code PRODUCTION_BASED} solo se permite renombrar.
 */
public record UpdatePayrollEmployeeCompensationCommand(
        UUID employeeId,
        String displayName,
        BigDecimal fixedAmount,
        LocalDate effectiveFrom,
        LocalDate effectiveTo
) {
}
