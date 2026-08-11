package com.magyen.platform.finance.application.dto;

/**
 * Consulta de listado de obligaciones financieras recurrentes.
 * <p>
 * {@code activeOnly = true} limita el resultado a obligaciones activas.
 * {@code null} o {@code false} lista todas.
 */
public record GetRecurringFinancialObligationsQuery(
        Boolean activeOnly
) {
    public static GetRecurringFinancialObligationsQuery all() {
        return new GetRecurringFinancialObligationsQuery(null);
    }
}
