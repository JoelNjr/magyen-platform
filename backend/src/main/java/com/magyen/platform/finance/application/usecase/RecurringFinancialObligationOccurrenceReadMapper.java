package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationOccurrenceResult;
import com.magyen.platform.finance.application.dto.GetRecurringFinancialObligationOccurrenceResult;
import com.magyen.platform.finance.domain.RecurringFinancialObligationOccurrence;

/**
 * Traduce ocurrencias de dominio a resultados de aplicación.
 */
final class RecurringFinancialObligationOccurrenceReadMapper {

    private RecurringFinancialObligationOccurrenceReadMapper() {
    }

    static GetRecurringFinancialObligationOccurrenceResult toGetResult(
            RecurringFinancialObligationOccurrence occurrence
    ) {
        return new GetRecurringFinancialObligationOccurrenceResult(
                occurrence.getId(),
                occurrence.getRecurringObligationId(),
                occurrence.getDueDate(),
                occurrence.getExpectedAmount().getValue(),
                occurrence.getStatus(),
                occurrence.getPaidDate(),
                occurrence.getFinancialTransactionId(),
                occurrence.getObservation()
        );
    }

    static CreateRecurringFinancialObligationOccurrenceResult toCreateResult(
            RecurringFinancialObligationOccurrence occurrence
    ) {
        return new CreateRecurringFinancialObligationOccurrenceResult(
                occurrence.getId(),
                occurrence.getRecurringObligationId(),
                occurrence.getDueDate(),
                occurrence.getExpectedAmount().getValue(),
                occurrence.getStatus(),
                occurrence.getPaidDate(),
                occurrence.getFinancialTransactionId(),
                occurrence.getObservation()
        );
    }
}
