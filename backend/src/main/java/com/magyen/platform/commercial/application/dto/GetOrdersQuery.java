package com.magyen.platform.commercial.application.dto;

import java.time.LocalDate;

/**
 * Filtro opcional de pedidos por fecha de confirmación.
 * <p>
 * Sin fechas se listan todos. No restringe de forma permanente al mes actual.
 */
public record GetOrdersQuery(
        LocalDate fromDate,
        LocalDate toDate
) {
}
