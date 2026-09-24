package com.magyen.platform.commercial.application.dto;

import java.time.LocalDate;

/**
 * Período inclusive para el listado de rentabilidad individual.
 * <p>
 * Sin fechas se usa el mes calendario actual. No usa entrega programada de Home.
 */
public record GetOrderProfitabilityListQuery(
        LocalDate fromDate,
        LocalDate toDate
) {
}
