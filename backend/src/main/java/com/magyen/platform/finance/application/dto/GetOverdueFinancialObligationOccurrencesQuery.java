package com.magyen.platform.finance.application.dto;

/**
 * Consulta de ocurrencias PENDING vencidas (dueDate &lt; hoy).
 */
public record GetOverdueFinancialObligationOccurrencesQuery() {

    public static GetOverdueFinancialObligationOccurrencesQuery create() {
        return new GetOverdueFinancialObligationOccurrencesQuery();
    }
}
