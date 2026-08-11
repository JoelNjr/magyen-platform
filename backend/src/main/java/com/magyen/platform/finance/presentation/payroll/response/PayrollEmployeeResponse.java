package com.magyen.platform.finance.presentation.payroll.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Representación HTTP de un empleado de nómina.
 */
public record PayrollEmployeeResponse(
        UUID employeeId,
        String displayName,
        boolean active,
        String compensationType,
        BigDecimal fixedAmount,
        String frequency,
        LocalDate effectiveFrom,
        LocalDate effectiveTo
) {
}
