package com.magyen.platform.commercial.application.dto;

import java.util.List;

/**
 * Listado de rentabilidad individual más el mismo resumen ponderado que Home.
 */
public record GetOrderProfitabilityListResult(
        List<GetOrderProfitabilityResult> orders,
        OrderProfitabilitySummary summary
) {
}
