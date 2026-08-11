package com.magyen.platform.finance.presentation.obligation.response;

import java.util.List;

/**
 * Respuesta HTTP del listado de obligaciones financieras recurrentes.
 */
public record GetRecurringFinancialObligationsResponse(
        List<RecurringFinancialObligationResponse> obligations
) {
}
