package com.magyen.platform.finance.presentation.payroll.request;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Payload HTTP para actualizar compensación / nombre de empleado de nómina.
 */
public record UpdatePayrollEmployeeCompensationRequest(
        String displayName,
        BigDecimal fixedAmount,
        LocalDate effectiveFrom,
        LocalDate effectiveTo
) {
}
