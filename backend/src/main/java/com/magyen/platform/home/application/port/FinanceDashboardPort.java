package com.magyen.platform.home.application.port;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Port de lectura financiera para el Dashboard Home.
 * <p>
 * Home no recalcula el ledger ni los compromisos: reutiliza los read models de Finance.
 */
public interface FinanceDashboardPort {

    FinancePeriodSummary getPeriodSummary(LocalDate fromDate, LocalDate toDate);

    /**
     * Compromisos financieros PENDING actuales (independientes del período del resumen).
     */
    HomeFinancialCommitmentsSnapshot getCurrentFinancialCommitments();

    /**
     * Resumen de movimientos reales del ledger en un período inclusivo.
     */
    record FinancePeriodSummary(
            LocalDate fromDate,
            LocalDate toDate,
            BigDecimal income,
            BigDecimal expense,
            BigDecimal netResult,
            long transactionCount
    ) {
    }

    /**
     * Snapshot de compromisos financieros para Home.
     */
    record HomeFinancialCommitmentsSnapshot(
            BigDecimal totalPendingAmount,
            BigDecimal totalOverdueAmount,
            int overdueCount,
            int upcomingCount,
            List<CommitmentItem> items
    ) {
    }

    /**
     * Ocurrencia PENDING enriquecida para el Dashboard.
     */
    record CommitmentItem(
            UUID occurrenceId,
            UUID obligationId,
            String name,
            String type,
            BigDecimal expectedAmount,
            LocalDate dueDate,
            String status,
            boolean overdue,
            Integer daysUntilDue,
            Integer daysOverdue
    ) {
    }
}
