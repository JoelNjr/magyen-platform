package com.magyen.platform.finance.presentation.payroll.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representación HTTP de un período de nómina.
 */
public record PayrollPeriodResponse(
        UUID periodId,
        UUID employeeId,
        String employeeDisplayName,
        LocalDate periodStart,
        LocalDate periodEnd,
        LocalDate expectedPaymentDate,
        BigDecimal amountSnapshot,
        String status,
        LocalDate actualPaymentDate,
        LocalDateTime paidAt,
        UUID financialTransactionId
) {
}
