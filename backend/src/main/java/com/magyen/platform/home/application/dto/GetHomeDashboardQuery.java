package com.magyen.platform.home.application.dto;

import java.time.LocalDate;

/**
 * Consulta del Dashboard Home.
 * <p>
 * Si {@code fromDate} y {@code toDate} son ambos null, el caso de uso
 * aplica el mes calendario actual (misma semántica que Finance en frontend).
 */
public record GetHomeDashboardQuery(
        LocalDate fromDate,
        LocalDate toDate
) {
}
