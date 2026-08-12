package com.magyen.platform.home.application.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Sección de compromisos financieros del Dashboard Home (solo lectura).
 * <p>
 * Representa ocurrencias PENDING <strong>actuales</strong>, independientes del
 * período del resumen financiero.
 */
public record HomeCommitmentsSummary(
        BigDecimal totalPendingAmount,
        BigDecimal totalOverdueAmount,
        int overdueCount,
        int upcomingCount,
        List<HomeCommitmentItem> items
) {
}
