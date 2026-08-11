package com.magyen.platform.finance.application.dto;

import com.magyen.platform.finance.domain.PayrollPeriodStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Resultado de consulta de un período de nómina.
 */
public record GetPayrollPeriodResult(
        UUID periodId,
        UUID employeeId,
        String employeeDisplayName,
        LocalDate periodStart,
        LocalDate periodEnd,
        LocalDate expectedPaymentDate,
        BigDecimal amountSnapshot,
        PayrollPeriodStatus status,
        LocalDate actualPaymentDate,
        LocalDateTime paidAt,
        UUID financialTransactionId
) {
}
