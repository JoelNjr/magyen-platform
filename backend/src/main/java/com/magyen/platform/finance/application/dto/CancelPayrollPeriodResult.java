package com.magyen.platform.finance.application.dto;

import com.magyen.platform.finance.domain.PayrollPeriodStatus;

import java.util.UUID;

/**
 * Resultado de cancelación de un período de nómina.
 */
public record CancelPayrollPeriodResult(
        UUID periodId,
        PayrollPeriodStatus status
) {
}
