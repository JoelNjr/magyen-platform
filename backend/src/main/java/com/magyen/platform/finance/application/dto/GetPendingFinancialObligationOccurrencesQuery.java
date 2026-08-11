package com.magyen.platform.finance.application.dto;

/**
 * Consulta de ocurrencias PENDING (incluye vencidas y por vencer).
 */
public record GetPendingFinancialObligationOccurrencesQuery() {

    public static GetPendingFinancialObligationOccurrencesQuery create() {
        return new GetPendingFinancialObligationOccurrencesQuery();
    }
}
