package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.GetRecurringFinancialObligationOccurrenceQuery;
import com.magyen.platform.finance.application.dto.GetRecurringFinancialObligationOccurrenceResult;
import com.magyen.platform.finance.domain.RecurringFinancialObligationOccurrence;
import com.magyen.platform.finance.domain.RecurringFinancialObligationOccurrenceRepository;

import java.util.Objects;

/**
 * Consulta una ocurrencia de obligación recurrente por identidad.
 */
public class GetRecurringFinancialObligationOccurrenceUseCase {

    private final RecurringFinancialObligationOccurrenceRepository occurrenceRepository;

    public GetRecurringFinancialObligationOccurrenceUseCase(
            RecurringFinancialObligationOccurrenceRepository occurrenceRepository
    ) {
        this.occurrenceRepository = Objects.requireNonNull(
                occurrenceRepository,
                "Occurrence repository must not be null"
        );
    }

    public GetRecurringFinancialObligationOccurrenceResult execute(
            GetRecurringFinancialObligationOccurrenceQuery query
    ) {
        Objects.requireNonNull(query, "Query must not be null");
        Objects.requireNonNull(query.occurrenceId(), "Occurrence id must not be null");

        RecurringFinancialObligationOccurrence occurrence = occurrenceRepository
                .findById(query.occurrenceId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Recurring financial obligation occurrence not found: " + query.occurrenceId()
                ));

        return RecurringFinancialObligationOccurrenceReadMapper.toGetResult(occurrence);
    }
}
