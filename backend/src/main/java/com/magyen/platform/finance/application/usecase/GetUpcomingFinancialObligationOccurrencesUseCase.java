package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.FinancialObligationOccurrenceCommitmentResult;
import com.magyen.platform.finance.application.dto.GetUpcomingFinancialObligationOccurrencesQuery;
import com.magyen.platform.finance.application.dto.GetUpcomingFinancialObligationOccurrencesResult;
import com.magyen.platform.finance.domain.RecurringFinancialObligation;
import com.magyen.platform.finance.domain.RecurringFinancialObligationOccurrenceRepository;
import com.magyen.platform.finance.domain.RecurringFinancialObligationRepository;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Consulta ocurrencias PENDING con vencimiento entre hoy y hoy+daysAhead.
 * <p>
 * No incluye vencidas ({@code dueDate < hoy}).
 */
public class GetUpcomingFinancialObligationOccurrencesUseCase {

    static final int DEFAULT_DAYS_AHEAD = 7;
    private static final int MAX_DAYS_AHEAD = 366;

    private final RecurringFinancialObligationOccurrenceRepository occurrenceRepository;
    private final RecurringFinancialObligationRepository obligationRepository;
    private final Clock clock;

    public GetUpcomingFinancialObligationOccurrencesUseCase(
            RecurringFinancialObligationOccurrenceRepository occurrenceRepository,
            RecurringFinancialObligationRepository obligationRepository,
            Clock clock
    ) {
        this.occurrenceRepository = Objects.requireNonNull(
                occurrenceRepository,
                "Occurrence repository must not be null"
        );
        this.obligationRepository = Objects.requireNonNull(
                obligationRepository,
                "Obligation repository must not be null"
        );
        this.clock = Objects.requireNonNull(clock, "Clock must not be null");
    }

    public GetUpcomingFinancialObligationOccurrencesResult execute(
            GetUpcomingFinancialObligationOccurrencesQuery query
    ) {
        Objects.requireNonNull(query, "Query must not be null");

        int daysAhead = resolveDaysAhead(query.daysAhead());
        LocalDate today = LocalDate.now(clock);
        LocalDate toDate = today.plusDays(daysAhead);

        Map<UUID, RecurringFinancialObligation> obligationsById = obligationRepository.findAll().stream()
                .collect(Collectors.toMap(RecurringFinancialObligation::getId, Function.identity()));

        List<FinancialObligationOccurrenceCommitmentResult> occurrences =
                occurrenceRepository.findPendingDueBetween(today, toDate).stream()
                        .map(occurrence -> FinancialObligationOccurrenceCommitmentReadMapper.toCommitment(
                                occurrence,
                                obligationsById,
                                today
                        ))
                        .toList();

        return new GetUpcomingFinancialObligationOccurrencesResult(List.copyOf(occurrences));
    }

    private static int resolveDaysAhead(Integer daysAhead) {
        int resolved = daysAhead == null ? DEFAULT_DAYS_AHEAD : daysAhead;
        if (resolved < 0) {
            throw new FinanceDomainException("Days ahead must not be negative");
        }
        if (resolved > MAX_DAYS_AHEAD) {
            throw new FinanceDomainException("Days ahead must not exceed " + MAX_DAYS_AHEAD);
        }
        return resolved;
    }
}
