package com.magyen.platform.finance.application.dto;

import com.magyen.platform.finance.domain.PayrollDeductionStatus;
import com.magyen.platform.finance.domain.PayrollDeductionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Resultado de registrar un descuento de nómina.
 */
public record CreatePayrollDeductionResult(
        UUID deductionId,
        UUID employeeId,
        PayrollDeductionType type,
        BigDecimal amount,
        LocalDate deductionDate,
        String description,
        PayrollDeductionStatus status,
        LocalDateTime createdAt
) {
}
