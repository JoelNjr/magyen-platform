package com.magyen.platform.home.infrastructure.finance;

import com.magyen.platform.finance.application.dto.FinancialObligationOccurrenceCommitmentResult;
import com.magyen.platform.finance.application.dto.GetFinancialPeriodSummaryQuery;
import com.magyen.platform.finance.application.dto.GetFinancialPeriodSummaryResult;
import com.magyen.platform.finance.application.dto.GetOverdueFinancialObligationOccurrencesQuery;
import com.magyen.platform.finance.application.dto.GetOverdueFinancialObligationOccurrencesResult;
import com.magyen.platform.finance.application.dto.GetPendingFinancialObligationOccurrencesQuery;
import com.magyen.platform.finance.application.dto.GetPendingFinancialObligationOccurrencesResult;
import com.magyen.platform.finance.application.dto.GetUpcomingFinancialObligationOccurrencesQuery;
import com.magyen.platform.finance.application.dto.GetUpcomingFinancialObligationOccurrencesResult;
import com.magyen.platform.finance.application.usecase.GetFinancialPeriodSummaryUseCase;
import com.magyen.platform.finance.application.usecase.GetOverdueFinancialObligationOccurrencesUseCase;
import com.magyen.platform.finance.application.usecase.GetPendingFinancialObligationOccurrencesUseCase;
import com.magyen.platform.finance.application.usecase.GetUpcomingFinancialObligationOccurrencesUseCase;
import com.magyen.platform.home.application.port.FinanceDashboardPort;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Adaptador Home → Finance para resumen de período y compromisos PENDING.
 * <p>
 * Delega en los use cases de lectura de Finance; no duplica cálculos ni accede a JPA.
 * La ventana upcoming usa el default de Finance ({@code daysAhead = null} → 7 días).
 */
public class FinanceDashboardAdapter implements FinanceDashboardPort {

    private final GetFinancialPeriodSummaryUseCase getFinancialPeriodSummaryUseCase;
    private final GetPendingFinancialObligationOccurrencesUseCase getPendingFinancialObligationOccurrencesUseCase;
    private final GetOverdueFinancialObligationOccurrencesUseCase getOverdueFinancialObligationOccurrencesUseCase;
    private final GetUpcomingFinancialObligationOccurrencesUseCase getUpcomingFinancialObligationOccurrencesUseCase;

    public FinanceDashboardAdapter(
            GetFinancialPeriodSummaryUseCase getFinancialPeriodSummaryUseCase,
            GetPendingFinancialObligationOccurrencesUseCase getPendingFinancialObligationOccurrencesUseCase,
            GetOverdueFinancialObligationOccurrencesUseCase getOverdueFinancialObligationOccurrencesUseCase,
            GetUpcomingFinancialObligationOccurrencesUseCase getUpcomingFinancialObligationOccurrencesUseCase
    ) {
        this.getFinancialPeriodSummaryUseCase = Objects.requireNonNull(
                getFinancialPeriodSummaryUseCase,
                "Get financial period summary use case must not be null"
        );
        this.getPendingFinancialObligationOccurrencesUseCase = Objects.requireNonNull(
                getPendingFinancialObligationOccurrencesUseCase,
                "Get pending financial obligation occurrences use case must not be null"
        );
        this.getOverdueFinancialObligationOccurrencesUseCase = Objects.requireNonNull(
                getOverdueFinancialObligationOccurrencesUseCase,
                "Get overdue financial obligation occurrences use case must not be null"
        );
        this.getUpcomingFinancialObligationOccurrencesUseCase = Objects.requireNonNull(
                getUpcomingFinancialObligationOccurrencesUseCase,
                "Get upcoming financial obligation occurrences use case must not be null"
        );
    }

    @Override
    public FinancePeriodSummary getPeriodSummary(LocalDate fromDate, LocalDate toDate) {
        Objects.requireNonNull(fromDate, "From date must not be null");
        Objects.requireNonNull(toDate, "To date must not be null");

        GetFinancialPeriodSummaryResult result = getFinancialPeriodSummaryUseCase.execute(
                new GetFinancialPeriodSummaryQuery(fromDate, toDate)
        );

        return new FinancePeriodSummary(
                result.fromDate(),
                result.toDate(),
                result.totalIncome(),
                result.totalExpense(),
                result.netResult(),
                result.transactionCount()
        );
    }

    @Override
    public HomeFinancialCommitmentsSnapshot getCurrentFinancialCommitments() {
        GetPendingFinancialObligationOccurrencesResult pending =
                getPendingFinancialObligationOccurrencesUseCase.execute(
                        GetPendingFinancialObligationOccurrencesQuery.create()
                );
        GetOverdueFinancialObligationOccurrencesResult overdue =
                getOverdueFinancialObligationOccurrencesUseCase.execute(
                        GetOverdueFinancialObligationOccurrencesQuery.create()
                );
        // null daysAhead → Finance DEFAULT_DAYS_AHEAD (7)
        GetUpcomingFinancialObligationOccurrencesResult upcoming =
                getUpcomingFinancialObligationOccurrencesUseCase.execute(
                        new GetUpcomingFinancialObligationOccurrencesQuery(null)
                );

        List<CommitmentItem> items = pending.occurrences().stream()
                .map(this::toCommitmentItem)
                .toList();

        return new HomeFinancialCommitmentsSnapshot(
                pending.totalPendingAmount(),
                overdue.totalOverdueAmount(),
                overdue.occurrences().size(),
                upcoming.occurrences().size(),
                items
        );
    }

    private CommitmentItem toCommitmentItem(FinancialObligationOccurrenceCommitmentResult occurrence) {
        return new CommitmentItem(
                occurrence.occurrenceId(),
                occurrence.recurringObligationId(),
                occurrence.obligationName(),
                occurrence.obligationType() == null ? null : occurrence.obligationType().name(),
                occurrence.expectedAmount(),
                occurrence.dueDate(),
                occurrence.status() == null ? null : occurrence.status().name(),
                occurrence.overdue(),
                occurrence.daysUntilDue(),
                occurrence.daysOverdue()
        );
    }
}
