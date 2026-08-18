package com.magyen.platform.finance.application.port;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Puerto Finance → Commercial para acumulación analítica de comisión de vendedor.
 * <p>
 * Finance no persiste pedidos ni comisiones.
 */
public interface EmployeeSellerCommissionsPort {

    EmployeeSellerCommissionsSnapshot findCommissions(UUID sellerEmployeeId, LocalDate fromDate, LocalDate toDate);

    record EmployeeSellerCommissionsSnapshot(
            UUID sellerEmployeeId,
            LocalDate fromDate,
            LocalDate toDate,
            int numberOfEligibleOrders,
            BigDecimal totalSales,
            BigDecimal commissionRate,
            BigDecimal accumulatedCommission
    ) {
    }
}
