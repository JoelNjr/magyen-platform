package com.magyen.platform.finance.application.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Resumen de una generación controlada de períodos de nómina.
 */
public record GeneratePayrollPeriodsResult(
        LocalDate requestedFrom,
        LocalDate requestedTo,
        int employeesEvaluated,
        int created,
        int alreadyExisting,
        int skippedInactive,
        int skippedProductionBased,
        int skippedOutsideValidity,
        List<GetPayrollPeriodResult> createdPeriods
) {
}
