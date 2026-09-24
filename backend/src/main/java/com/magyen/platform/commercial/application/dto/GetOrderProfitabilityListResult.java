package com.magyen.platform.commercial.application.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Listado de rentabilidad individual más el mismo resumen ponderado que Home.
 * <p>
 * {@code fromDate}/{@code toDate} son el período aplicado por el backend.
 */
public record GetOrderProfitabilityListResult(
        List<GetOrderProfitabilityResult> orders,
        OrderProfitabilitySummary summary,
        LocalDate fromDate,
        LocalDate toDate
) {
}
