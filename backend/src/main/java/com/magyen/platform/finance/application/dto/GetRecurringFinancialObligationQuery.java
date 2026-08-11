package com.magyen.platform.finance.application.dto;

import java.util.UUID;

/**
 * Consulta de una obligación financiera recurrente por identidad.
 */
public record GetRecurringFinancialObligationQuery(
        UUID obligationId
) {
}
