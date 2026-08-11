package com.magyen.platform.finance.presentation.payroll.response;

import java.time.LocalDate;
import java.util.List;

/**
 * Respuesta HTTP del resumen de generación de períodos de nómina.
 */
public record GeneratePayrollPeriodsResponse(
        LocalDate requestedFrom,
        LocalDate requestedTo,
        int employeesEvaluated,
        int created,
        int alreadyExisting,
        int skippedInactive,
        int skippedProductionBased,
        int skippedOutsideValidity,
        List<PayrollPeriodResponse> createdPeriods
) {
}
