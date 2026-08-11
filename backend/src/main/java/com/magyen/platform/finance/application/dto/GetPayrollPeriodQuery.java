package com.magyen.platform.finance.application.dto;

import java.util.UUID;

/**
 * Consulta de un período de nómina por identidad.
 */
public record GetPayrollPeriodQuery(
        UUID periodId
) {
}
