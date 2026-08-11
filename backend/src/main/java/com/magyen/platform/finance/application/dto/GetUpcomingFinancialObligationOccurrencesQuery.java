package com.magyen.platform.finance.application.dto;

/**
 * Consulta de ocurrencias PENDING con vencimiento hoy o en los próximos {@code daysAhead} días.
 */
public record GetUpcomingFinancialObligationOccurrencesQuery(
        Integer daysAhead
) {
}
