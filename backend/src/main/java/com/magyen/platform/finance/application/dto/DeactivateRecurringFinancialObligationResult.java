package com.magyen.platform.finance.application.dto;

import java.util.UUID;

/**
 * Resultado de desactivación de una obligación financiera recurrente.
 */
public record DeactivateRecurringFinancialObligationResult(
        UUID obligationId,
        boolean active
) {
}
