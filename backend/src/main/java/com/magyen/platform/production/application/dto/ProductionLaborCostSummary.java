package com.magyen.platform.production.application.dto;

import java.math.BigDecimal;

/**
 * Resumen de costo de mano de obra de una Orden de Producción.
 * <p>
 * {@code totalLaborCost} suma PENDING + PAID. Es null cuando no hay registros no cancelados
 * (para distinguir "No hay mano de obra registrada" de un total valorizado).
 */
public record ProductionLaborCostSummary(
        BigDecimal totalLaborCost,
        int laborWorkCount,
        int pendingCount,
        int paidCount
) {
}
