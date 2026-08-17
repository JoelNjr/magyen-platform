package com.magyen.platform.finance.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Listado de descuentos de un empleado, con totales solo de descuentos ACTIVE.
 */
public record GetPayrollDeductionsResult(
        UUID employeeId,
        String employeeDisplayName,
        List<PayrollDeductionResult> deductions,
        int activeCount,
        BigDecimal activeTotal
) {
}
