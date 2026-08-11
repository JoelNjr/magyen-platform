package com.magyen.platform.finance.application.dto;

import java.util.List;

/**
 * Resultado del listado de obligaciones financieras recurrentes.
 */
public record GetRecurringFinancialObligationsResult(
        List<GetRecurringFinancialObligationResult> obligations
) {
}
