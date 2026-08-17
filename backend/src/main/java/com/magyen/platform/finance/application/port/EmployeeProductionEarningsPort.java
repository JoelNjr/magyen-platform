package com.magyen.platform.finance.application.port;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Puerto Finance → Production para acumulación de mano de obra.
 * <p>
 * Finance no persiste ProductionLaborWork.
 */
public interface EmployeeProductionEarningsPort {

    EmployeeProductionEarningsSnapshot findEarnings(UUID employeeId, LocalDate fromDate, LocalDate toDate);

    record EmployeeProductionEarningsSnapshot(
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
}
