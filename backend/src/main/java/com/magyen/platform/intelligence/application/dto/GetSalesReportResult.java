package com.magyen.platform.intelligence.application.dto;

import java.math.BigDecimal;

/**
 * Resultado del caso de uso de reporte de ventas.
 */
public record GetSalesReportResult(
        BigDecimal totalSold,
        long orderCount,
        BigDecimal averagePerSale
) {
}
