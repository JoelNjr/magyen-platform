package com.magyen.platform.finance.application.dto;

import com.magyen.platform.finance.domain.PayrollDeductionStatus;

import java.util.UUID;

/**
 * Resultado de cancelar un descuento de nómina.
 */
public record CancelPayrollDeductionResult(
        UUID deductionId,
        UUID employeeId,
        PayrollDeductionStatus status
) {
}
