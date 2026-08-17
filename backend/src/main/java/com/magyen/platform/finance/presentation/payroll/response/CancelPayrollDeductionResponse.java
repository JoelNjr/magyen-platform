package com.magyen.platform.finance.presentation.payroll.response;

import java.util.UUID;

/**
 * Confirmación HTTP de cancelación de un descuento.
 */
public record CancelPayrollDeductionResponse(
        UUID deductionId,
        UUID employeeId,
        String status
) {
}
