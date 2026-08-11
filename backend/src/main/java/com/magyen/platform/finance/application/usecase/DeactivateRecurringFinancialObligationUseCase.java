package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.DeactivateRecurringFinancialObligationCommand;
import com.magyen.platform.finance.application.dto.DeactivateRecurringFinancialObligationResult;
import com.magyen.platform.finance.domain.RecurringFinancialObligation;
import com.magyen.platform.finance.domain.RecurringFinancialObligationRepository;

import java.util.Objects;

/**
 * Caso de uso que desactiva una obligación financiera recurrente.
 * <p>
 * La obligación permanece legible históricamente. No elimina datos ni crea
 * movimientos del ledger.
 */
public class DeactivateRecurringFinancialObligationUseCase {

    private final RecurringFinancialObligationRepository recurringFinancialObligationRepository;

    public DeactivateRecurringFinancialObligationUseCase(
            RecurringFinancialObligationRepository recurringFinancialObligationRepository
    ) {
        this.recurringFinancialObligationRepository = Objects.requireNonNull(
                recurringFinancialObligationRepository,
                "Recurring financial obligation repository must not be null"
        );
    }

    public DeactivateRecurringFinancialObligationResult execute(
            DeactivateRecurringFinancialObligationCommand command
    ) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.obligationId(), "Obligation id must not be null");

        RecurringFinancialObligation obligation = recurringFinancialObligationRepository
                .findById(command.obligationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Recurring financial obligation not found: " + command.obligationId()
                ));

        obligation.deactivate();
        RecurringFinancialObligation saved = recurringFinancialObligationRepository.update(obligation);

        return new DeactivateRecurringFinancialObligationResult(
                saved.getId(),
                saved.isActive()
        );
    }
}
