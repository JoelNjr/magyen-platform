package com.magyen.platform.finance.application.port;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Puerto Finance → Commercial para la comisión analítica mensual de un vendedor.
 * <p>
 * Finance no persiste pedidos ni comisiones y no crea asientos.
 */
public interface EmployeeSellerCommissionsPort {

    EmployeeSellerCommissionsSnapshot findCommissions(UUID sellerEmployeeId, LocalDate fromDate, LocalDate toDate);

    List<EmployeeSellerCommissionsSnapshot> findCommissionsForMonth(LocalDate fromDate, LocalDate toDate);

    EmployeeSellerCommissionsSnapshot findUnboundedCommissions(UUID sellerEmployeeId);

    record CommissionOrderLine(
            UUID orderId,
            String orderNumber,
            String customerName,
            LocalDate confirmationDate,
            BigDecimal orderTotal,
            BigDecimal commissionRate,
            BigDecimal commissionAmount
    ) {
    }

    record EmployeeSellerCommissionsSnapshot(
            UUID sellerEmployeeId,
            LocalDate fromDate,
            LocalDate toDate,
            int numberOfEligibleOrders,
            BigDecimal totalSales,
            BigDecimal commissionRate,
            BigDecimal accumulatedCommission,
            String settlementStatus,
            List<CommissionOrderLine> orders
    ) {
    }
}
