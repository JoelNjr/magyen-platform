package com.magyen.platform.finance.presentation.payroll.request;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Payload HTTP para crear un empleado de nómina.
 */
public record CreatePayrollEmployeeRequest(
        String displayName,
        String compensationType,
        BigDecimal fixedAmount,
        LocalDate effectiveFrom,
        LocalDate effectiveTo
) {
}
