package com.magyen.platform.finance.application.dto;

import com.magyen.platform.finance.domain.PayrollCompensationType;
import com.magyen.platform.finance.domain.PayrollFrequency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Resultado de consulta de un empleado de nómina.
 */
public record GetPayrollEmployeeResult(
        UUID employeeId,
        String displayName,
        boolean active,
        PayrollCompensationType compensationType,
        BigDecimal fixedAmount,
        PayrollFrequency frequency,
        LocalDate effectiveFrom,
        LocalDate effectiveTo
) {
}
