package com.magyen.platform.finance.presentation.payroll.request;

import java.time.LocalDateTime;

/**
 * Payload HTTP opcional para pagar un período de nómina.
 */
public record PayPayrollPeriodRequest(
        LocalDateTime paidAt,
        String observation
) {
}
