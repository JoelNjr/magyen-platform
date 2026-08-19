package com.magyen.platform.commercial.application.dto;

import java.time.LocalDate;

/**
 * Filtro opcional de cotizaciones por fecha de creación.
 * <p>
 * Sin fechas se listan todas. No restringe de forma permanente al mes actual.
 */
public record GetQuotationsQuery(
        LocalDate fromDate,
        LocalDate toDate
) {
}
