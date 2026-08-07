package com.magyen.platform.intelligence.application.dto;

import com.magyen.platform.production.domain.ProductionStatus;

import java.util.Map;

/**
 * Resultado del caso de uso de reporte de producción.
 * <p>
 * Las cantidades se agrupan por {@link ProductionStatus} del dominio de producción.
 */
public record GetProductionReportResult(
        Map<ProductionStatus, Long> countByStatus
) {
}
