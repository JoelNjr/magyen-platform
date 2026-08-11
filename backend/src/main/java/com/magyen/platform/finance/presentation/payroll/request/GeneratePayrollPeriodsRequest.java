package com.magyen.platform.finance.presentation.payroll.request;

import java.time.LocalDate;

/**
 * Payload HTTP para generación controlada de períodos de nómina.
 */
public record GeneratePayrollPeriodsRequest(
        LocalDate fromDate,
        LocalDate toDate
) {
}
