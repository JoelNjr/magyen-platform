package com.magyen.platform.intelligence.presentation.report.response;

import java.util.Map;

/**
 * Respuesta HTTP del reporte de producción.
 * <p>
 * Las claves del mapa corresponden a los nombres de {@code ProductionStatus}.
 */
public record GetProductionReportResponse(
        Map<String, Long> countByStatus
) {
}
