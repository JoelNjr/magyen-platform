package com.magyen.platform.finance.presentation.payroll.request;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Payload HTTP para registrar un descuento de nómina.
 */
public record CreatePayrollDeductionRequest(
        String type,
        BigDecimal amount,
        LocalDate deductionDate,
        String description
) {
}
