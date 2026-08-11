package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.FinancialObligationOccurrenceCommitmentResult;
import com.magyen.platform.finance.application.dto.GetOverdueFinancialObligationOccurrencesQuery;
import com.magyen.platform.finance.application.dto.GetOverdueFinancialObligationOccurrencesResult;
import com.magyen.platform.finance.domain.RecurringFinancialObligation;
import com.magyen.platform.finance.domain.RecurringFinancialObligationOccurrenceRepository;
import com.magyen.platform.finance.domain.RecurringFinancialObligationRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Consulta ocurrencias PENDING vencidas ({@code dueDate < hoy}) y su total.
 * <p>
 * La fecha de vencimiento de la ocurrencia es autoritativa. No usa el ledger.
 */
public class GetOverdueFinancialObligationOccurrencesUseCase {

    private final RecurringFinancialObligationOccurrenceRepository occurrenceRepository;
    private final RecurringFinancialObligationRepository obligationRepository;
    private final Clock clock;

    public GetOverdueFinancialObligationOccurrencesUseCase(
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

    public GetOverdueFinancialObligationOccurrencesResult execute(
            GetOverdueFinancialObligationOccurrencesQuery query
    ) {
        Objects.requireNonNull(query, "Query must not be null");

        LocalDate today = LocalDate.now(clock);
        Map<UUID, RecurringFinancialObligation> obligationsById = obligationRepository.findAll().stream()
                .collect(Collectors.toMap(RecurringFinancialObligation::getId, Function.identity()));

        List<FinancialObligationOccurrenceCommitmentResult> occurrences =
                occurrenceRepository.findPendingDueBefore(today).stream()
                        .map(occurrence -> FinancialObligationOccurrenceCommitmentReadMapper.toCommitment(
                                occurrence,
                                obligationsById,
                                today
                        ))
                        .toList();

        return new GetOverdueFinancialObligationOccurrencesResult(
                List.copyOf(occurrences),
                occurrenceRepository.sumPendingExpectedAmountDueBefore(today)
        );
    }
}
