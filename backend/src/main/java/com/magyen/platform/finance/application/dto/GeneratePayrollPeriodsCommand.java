package com.magyen.platform.finance.application.dto;

import java.time.LocalDate;

/**
 * Entrada del caso de uso de generación controlada de períodos de nómina.
 */
public record GeneratePayrollPeriodsCommand(
        LocalDate fromDate,
        LocalDate toDate
) {
}
