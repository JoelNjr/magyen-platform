package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.GetRecurringFinancialObligationsQuery;
import com.magyen.platform.finance.application.dto.GetRecurringFinancialObligationsResult;
import com.magyen.platform.finance.domain.RecurringFinancialObligation;
import com.magyen.platform.finance.domain.RecurringFinancialObligationRepository;

import java.util.List;
import java.util.Objects;

/**
 * Caso de uso que lista obligaciones financieras recurrentes.
 */
public class GetRecurringFinancialObligationsUseCase {

    private final RecurringFinancialObligationRepository recurringFinancialObligationRepository;

    public GetRecurringFinancialObligationsUseCase(
            RecurringFinancialObligationRepository recurringFinancialObligationRepository
    ) {
        this.recurringFinancialObligationRepository = Objects.requireNonNull(
                recurringFinancialObligationRepository,
                "Recurring financial obligation repository must not be null"
        );
    }

    public GetRecurringFinancialObligationsResult execute() {
        return execute(GetRecurringFinancialObligationsQuery.all());
    }

    public GetRecurringFinancialObligationsResult execute(GetRecurringFinancialObligationsQuery query) {
        Objects.requireNonNull(query, "Query must not be null");

        List<RecurringFinancialObligation> obligations = Boolean.TRUE.equals(query.activeOnly())
                ? recurringFinancialObligationRepository.findActive()
                : recurringFinancialObligationRepository.findAll();

        return new GetRecurringFinancialObligationsResult(
                obligations.stream()
                        .map(RecurringFinancialObligationReadMapper::toGetResult)
                        .toList()
        );
    }
}
