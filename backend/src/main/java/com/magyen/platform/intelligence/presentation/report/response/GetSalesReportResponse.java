package com.magyen.platform.intelligence.presentation.report.response;

import java.math.BigDecimal;

/**
 * Respuesta HTTP del reporte de ventas.
 */
public record GetSalesReportResponse(
        BigDecimal totalSold,
        long orderCount,
        BigDecimal averagePerSale
) {
}
