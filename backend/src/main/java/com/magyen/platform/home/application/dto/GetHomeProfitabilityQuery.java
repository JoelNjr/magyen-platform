package com.magyen.platform.home.application.dto;

import java.time.LocalDate;

/**
 * Consulta de rentabilidad Home por período de entrega programada.
 * <p>
 * Si ambas fechas son null, el caso de uso aplica el mes calendario actual.
 */
public record GetHomeProfitabilityQuery(
        LocalDate fromDate,
        LocalDate toDate
) {
}
