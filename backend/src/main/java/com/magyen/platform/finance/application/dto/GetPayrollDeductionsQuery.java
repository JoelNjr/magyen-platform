package com.magyen.platform.finance.application.dto;

import com.magyen.platform.finance.domain.PayrollDeductionStatus;

import java.util.UUID;

/**
 * Consulta de descuentos de un empleado.
 * <p>
 * {@code status} nulo lista el historial completo.
 */
public record GetPayrollDeductionsQuery(
        UUID employeeId,
        PayrollDeductionStatus status
) {
}
