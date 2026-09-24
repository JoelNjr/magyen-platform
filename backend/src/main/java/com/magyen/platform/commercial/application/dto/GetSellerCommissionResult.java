package com.magyen.platform.commercial.application.dto;

import com.magyen.platform.commercial.domain.SellerCommissionReadStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Comisión mensual de un vendedor. No es un asiento Finance ni una liquidación.
 */
public record GetSellerCommissionResult(
        UUID sellerEmployeeId,
        LocalDate fromDate,
        LocalDate toDate,
        int numberOfEligibleOrders,
        BigDecimal totalSales,
        BigDecimal commissionRate,
        BigDecimal accumulatedCommission,
        SellerCommissionReadStatus settlementStatus,
        List<SellerCommissionOrderLine> orders
) {
}
