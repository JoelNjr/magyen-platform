package com.magyen.platform.home.presentation.dashboard.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Sección HTTP de cuentas por cobrar del Dashboard Home.
 */
public record HomeReceivablesResponse(
        BigDecimal totalOutstandingAmount,
        BigDecimal totalCollectedAmount,
        int orderCount,
        List<HomeReceivableItemResponse> items
) {
}
