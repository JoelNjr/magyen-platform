package com.magyen.platform.finance.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entrada del caso de uso para pagar un período de nómina pendiente.
 * <p>
 * {@code paidAt} es opcional; si se omite se usa la marca de tiempo actual.
 */
public record PayPayrollPeriodCommand(
        UUID periodId,
        LocalDateTime paidAt,
        String observation
) {
    public PayPayrollPeriodCommand(UUID periodId) {
        this(periodId, null, null);
    }
}
