package com.magyen.platform.finance.application.dto;

import java.util.UUID;

/**
 * Entrada del caso de uso para desactivar una obligación financiera recurrente.
 */
public record DeactivateRecurringFinancialObligationCommand(
        UUID obligationId
) {
}
