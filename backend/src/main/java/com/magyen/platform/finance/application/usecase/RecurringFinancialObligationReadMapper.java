package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationResult;
import com.magyen.platform.finance.application.dto.GetRecurringFinancialObligationResult;
import com.magyen.platform.finance.application.dto.UpdateRecurringFinancialObligationResult;
import com.magyen.platform.finance.domain.RecurringFinancialObligation;

/**
 * Traduce el agregado de dominio a resultados de aplicación.
 */
final class RecurringFinancialObligationReadMapper {

    private RecurringFinancialObligationReadMapper() {
    }

    static GetRecurringFinancialObligationResult toGetResult(RecurringFinancialObligation obligation) {
        return new GetRecurringFinancialObligationResult(
                obligation.getId(),
                obligation.getName(),
                obligation.getType(),
                obligation.getExpectedAmount().getValue(),
                obligation.getFrequency(),
                obligation.getDueDay(),
                obligation.getStartDate(),
                obligation.getEndDate(),
                obligation.isActive(),
                obligation.getDescription(),
                obligation.getObservation()
        );
    }

    static CreateRecurringFinancialObligationResult toCreateResult(RecurringFinancialObligation obligation) {
        return new CreateRecurringFinancialObligationResult(
                obligation.getId(),
                obligation.getName(),
                obligation.getType(),
                obligation.getExpectedAmount().getValue(),
                obligation.getFrequency(),
                obligation.getDueDay(),
                obligation.getStartDate(),
                obligation.getEndDate(),
                obligation.isActive(),
                obligation.getDescription(),
                obligation.getObservation()
        );
    }

    static UpdateRecurringFinancialObligationResult toUpdateResult(RecurringFinancialObligation obligation) {
        return new UpdateRecurringFinancialObligationResult(
                obligation.getId(),
                obligation.getName(),
                obligation.getType(),
                obligation.getExpectedAmount().getValue(),
                obligation.getFrequency(),
                obligation.getDueDay(),
                obligation.getStartDate(),
                obligation.getEndDate(),
                obligation.isActive(),
                obligation.getDescription(),
                obligation.getObservation()
        );
    }
}
