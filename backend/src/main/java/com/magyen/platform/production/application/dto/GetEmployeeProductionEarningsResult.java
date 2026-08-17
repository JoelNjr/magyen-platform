package com.magyen.platform.production.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Acumulación de mano de obra de un empleado en un rango de fechas.
 * <p>
 * CANCELLED no entra. PENDING cuenta como generado no pagado. PAID cuenta como generado y pagado.
 */
public record GetEmployeeProductionEarningsResult(
        UUID employeeId,
        LocalDate fromDate,
        LocalDate toDate,
        int laborWorkCount,
        BigDecimal totalQuantity,
        BigDecimal totalCalculatedAmount,
        BigDecimal totalPaidAmount,
        BigDecimal totalPendingAmount
) {
}
