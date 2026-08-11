package com.magyen.platform.finance.application.dto;

import java.util.UUID;

/**
 * Entrada del caso de uso para cancelar un período de nómina pendiente.
 */
public record CancelPayrollPeriodCommand(
        UUID periodId
) {
}
