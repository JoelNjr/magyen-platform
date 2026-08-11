package com.magyen.platform.finance.presentation.obligation.response;

import java.util.UUID;

/**
 * Respuesta HTTP tras desactivar una obligación financiera recurrente.
 */
public record DeactivateRecurringFinancialObligationResponse(
        UUID obligationId,
        boolean active
) {
}
