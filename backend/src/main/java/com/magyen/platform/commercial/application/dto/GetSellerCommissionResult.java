package com.magyen.platform.commercial.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Acumulación analítica de comisión. No es un asiento Finance.
 */
public record GetSellerCommissionResult(
        UUID sellerEmployeeId,
        LocalDate fromDate,
        LocalDate toDate,
        int numberOfEligibleOrders,
        BigDecimal totalSales,
        BigDecimal commissionRate,
        BigDecimal accumulatedCommission
) {
}
