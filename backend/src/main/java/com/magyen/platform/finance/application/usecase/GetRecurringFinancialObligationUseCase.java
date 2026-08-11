package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.GetRecurringFinancialObligationQuery;
import com.magyen.platform.finance.application.dto.GetRecurringFinancialObligationResult;
import com.magyen.platform.finance.domain.RecurringFinancialObligation;
import com.magyen.platform.finance.domain.RecurringFinancialObligationRepository;

import java.util.Objects;

/**
 * Caso de uso que consulta una obligación financiera recurrente por identidad.
 */
public class GetRecurringFinancialObligationUseCase {

    private final RecurringFinancialObligationRepository recurringFinancialObligationRepository;

    public GetRecurringFinancialObligationUseCase(
            RecurringFinancialObligationRepository recurringFinancialObligationRepository
    ) {
        this.recurringFinancialObligationRepository = Objects.requireNonNull(
                recurringFinancialObligationRepository,
                "Recurring financial obligation repository must not be null"
        );
    }

    public GetRecurringFinancialObligationResult execute(GetRecurringFinancialObligationQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        Objects.requireNonNull(query.obligationId(), "Obligation id must not be null");

        RecurringFinancialObligation obligation = recurringFinancialObligationRepository
                .findById(query.obligationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Recurring financial obligation not found: " + query.obligationId()
                ));

        return RecurringFinancialObligationReadMapper.toGetResult(obligation);
    }
}
