package com.magyen.platform.home.presentation.dashboard.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Sección HTTP de compromisos financieros del Dashboard Home.
 */
public record HomeCommitmentsResponse(
        BigDecimal totalPendingAmount,
        BigDecimal totalOverdueAmount,
        int overdueCount,
        int upcomingCount,
        List<HomeCommitmentItemResponse> items
) {
}
