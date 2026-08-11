package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.FinancialObligationOccurrenceCommitmentResult;
import com.magyen.platform.finance.application.dto.GetPendingFinancialObligationOccurrencesQuery;
import com.magyen.platform.finance.application.dto.GetPendingFinancialObligationOccurrencesResult;
import com.magyen.platform.finance.domain.RecurringFinancialObligation;
import com.magyen.platform.finance.domain.RecurringFinancialObligationOccurrence;
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
 * Consulta ocurrencias PENDING (compromisos) y el total pendiente.
 * <p>
 * No usa {@code FinancialTransaction}. Incluye vencidas y no vencidas.
 */
public class GetPendingFinancialObligationOccurrencesUseCase {

    private final RecurringFinancialObligationOccurrenceRepository occurrenceRepository;
    private final RecurringFinancialObligationRepository obligationRepository;
    private final Clock clock;

    public GetPendingFinancialObligationOccurrencesUseCase(
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

    public GetPendingFinancialObligationOccurrencesResult execute(
            GetPendingFinancialObligationOccurrencesQuery query
    ) {
        Objects.requireNonNull(query, "Query must not be null");

        LocalDate today = LocalDate.now(clock);
        Map<UUID, RecurringFinancialObligation> obligationsById = loadObligationsById();

        List<FinancialObligationOccurrenceCommitmentResult> occurrences =
                occurrenceRepository.findPendingOrdered().stream()
                        .map(occurrence -> toCommitment(occurrence, obligationsById, today))
                        .toList();

        return new GetPendingFinancialObligationOccurrencesResult(
                List.copyOf(occurrences),
                occurrenceRepository.sumPendingExpectedAmount()
        );
    }

    private Map<UUID, RecurringFinancialObligation> loadObligationsById() {
        return obligationRepository.findAll().stream()
                .collect(Collectors.toMap(RecurringFinancialObligation::getId, Function.identity()));
    }

    private static FinancialObligationOccurrenceCommitmentResult toCommitment(
            RecurringFinancialObligationOccurrence occurrence,
            Map<UUID, RecurringFinancialObligation> obligationsById,
            LocalDate today
    ) {
        return FinancialObligationOccurrenceCommitmentReadMapper.toCommitment(
                occurrence,
                obligationsById,
                today
        );
    }
}
