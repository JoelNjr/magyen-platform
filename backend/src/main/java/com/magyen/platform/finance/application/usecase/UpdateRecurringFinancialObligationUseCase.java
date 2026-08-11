package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.UpdateRecurringFinancialObligationCommand;
import com.magyen.platform.finance.application.dto.UpdateRecurringFinancialObligationResult;
import com.magyen.platform.finance.domain.FinancialAmount;
import com.magyen.platform.finance.domain.RecurringFinancialObligation;
import com.magyen.platform.finance.domain.RecurringFinancialObligationRepository;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;

import java.util.Objects;

/**
 * Caso de uso que actualiza los datos de una obligación financiera recurrente.
 * <p>
 * No altera el estado activo ni crea movimientos del ledger.
 */
public class UpdateRecurringFinancialObligationUseCase {

    private final RecurringFinancialObligationRepository recurringFinancialObligationRepository;

    public UpdateRecurringFinancialObligationUseCase(
            RecurringFinancialObligationRepository recurringFinancialObligationRepository
    ) {
        this.recurringFinancialObligationRepository = Objects.requireNonNull(
                recurringFinancialObligationRepository,
                "Recurring financial obligation repository must not be null"
        );
    }

    public UpdateRecurringFinancialObligationResult execute(UpdateRecurringFinancialObligationCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        RecurringFinancialObligation obligation = recurringFinancialObligationRepository
                .findById(command.obligationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Recurring financial obligation not found: " + command.obligationId()
                ));

        obligation.update(
                command.name(),
                command.type(),
                FinancialAmount.of(command.expectedAmount()),
                command.frequency(),
                command.dueDay(),
                command.startDate(),
                command.endDate(),
                command.description(),
                command.observation()
        );

        RecurringFinancialObligation saved = recurringFinancialObligationRepository.update(obligation);
        return RecurringFinancialObligationReadMapper.toUpdateResult(saved);
    }

    private void validateCommand(UpdateRecurringFinancialObligationCommand command) {
        if (command.obligationId() == null) {
            throw new FinanceDomainException("Obligation id must not be null");
        }
        if (command.name() == null || command.name().isBlank()) {
            throw new FinanceDomainException("Obligation name must not be blank");
        }
        if (command.type() == null) {
            throw new FinanceDomainException("Obligation type must not be null");
        }
        if (command.expectedAmount() == null) {
            throw new FinanceDomainException("Expected amount must not be null");
        }
        if (command.frequency() == null) {
            throw new FinanceDomainException("Frequency must not be null");
        }
        if (command.startDate() == null) {
            throw new FinanceDomainException("Start date must not be null");
        }
    }
}
