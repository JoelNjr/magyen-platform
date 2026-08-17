package com.magyen.platform.finance.presentation.payroll.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representación HTTP de un descuento de nómina.
 */
public record PayrollDeductionResponse(
        UUID deductionId,
        UUID employeeId,
        String type,
        BigDecimal amount,
        LocalDate deductionDate,
        String description,
        String status,
        LocalDateTime createdAt
) {
}
