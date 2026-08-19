package com.magyen.platform.production.application.dto;

import java.time.LocalDate;

/**
 * Filtro opcional de órdenes de producción por fecha de creación.
 * <p>
 * Sin fechas se listan todas. No restringe de forma permanente al mes actual.
 */
public record GetProductionOrdersQuery(
        LocalDate fromDate,
        LocalDate toDate
) {
}
