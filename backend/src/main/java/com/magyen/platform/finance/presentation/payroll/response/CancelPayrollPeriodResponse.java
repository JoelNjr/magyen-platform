package com.magyen.platform.finance.presentation.payroll.response;

import java.util.UUID;

/**
 * Respuesta HTTP de cancelación de un período de nómina.
 */
public record CancelPayrollPeriodResponse(
        UUID periodId,
        String status
) {
}
