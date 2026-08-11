package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.GetRecurringFinancialObligationOccurrencesQuery;
import com.magyen.platform.finance.application.dto.GetRecurringFinancialObligationOccurrencesResult;
import com.magyen.platform.finance.domain.RecurringFinancialObligationOccurrence;
import com.magyen.platform.finance.domain.RecurringFinancialObligationOccurrenceRepository;

import java.util.List;
import java.util.Objects;

/**
 * Lista ocurrencias de obligaciones recurrentes, opcionalmente filtradas por estado.
 */
public class GetRecurringFinancialObligationOccurrencesUseCase {

    private final RecurringFinancialObligationOccurrenceRepository occurrenceRepository;

    public GetRecurringFinancialObligationOccurrencesUseCase(
            RecurringFinancialObligationOccurrenceRepository occurrenceRepository
    ) {
        this.occurrenceRepository = Objects.requireNonNull(
                occurrenceRepository,
                "Occurrence repository must not be null"
        );
    }

    public GetRecurringFinancialObligationOccurrencesResult execute() {
        return execute(GetRecurringFinancialObligationOccurrencesQuery.all());
    }

    public GetRecurringFinancialObligationOccurrencesResult execute(
            GetRecurringFinancialObligationOccurrencesQuery query
    ) {
        Objects.requireNonNull(query, "Query must not be null");

        List<RecurringFinancialObligationOccurrence> occurrences = query.status() == null
                ? occurrenceRepository.findAll()
                : occurrenceRepository.findByStatus(query.status());

        return new GetRecurringFinancialObligationOccurrencesResult(
                occurrences.stream()
                        .map(RecurringFinancialObligationOccurrenceReadMapper::toGetResult)
                        .toList()
        );
    }
}
