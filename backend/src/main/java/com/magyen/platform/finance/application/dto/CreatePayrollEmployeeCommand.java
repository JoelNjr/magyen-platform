package com.magyen.platform.finance.application.dto;

import com.magyen.platform.finance.domain.PayrollCompensationType;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entrada del caso de uso para crear un empleado de nómina.
 */
public record CreatePayrollEmployeeCommand(
        String displayName,
        PayrollCompensationType compensationType,
        BigDecimal fixedAmount,
        LocalDate effectiveFrom,
        LocalDate effectiveTo
) {
}
