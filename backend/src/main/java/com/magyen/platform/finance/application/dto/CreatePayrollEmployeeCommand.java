package com.magyen.platform.finance.application.dto;

import com.magyen.platform.finance.domain.PayrollCompensationType;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entrada del caso de uso para crear un empleado de nómina.
 * <p>
 * {@code salesParticipant} nace en false. Un empleado de sueldo fijo no es vendedor
 * hasta que el negocio lo marque.
 */
public record CreatePayrollEmployeeCommand(
        String displayName,
        PayrollCompensationType compensationType,
        BigDecimal fixedAmount,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        boolean salesParticipant
) {
    public CreatePayrollEmployeeCommand(
            String displayName,
            PayrollCompensationType compensationType,
            BigDecimal fixedAmount,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {
        this(displayName, compensationType, fixedAmount, effectiveFrom, effectiveTo, false);
    }
}
