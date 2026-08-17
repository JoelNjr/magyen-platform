package com.magyen.platform.finance.presentation.payroll.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Listado HTTP de descuentos de un empleado.
 */
public record GetPayrollDeductionsResponse(
        UUID employeeId,
        String employeeDisplayName,
        List<PayrollDeductionResponse> deductions,
        int activeCount,
        BigDecimal activeTotal
) {
}
