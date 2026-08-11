package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationCommand;
import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationResult;
import com.magyen.platform.finance.domain.FinancialAmount;
import com.magyen.platform.finance.domain.RecurringFinancialObligation;
import com.magyen.platform.finance.domain.RecurringFinancialObligationRepository;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;

import java.util.Objects;

/**
 * Caso de uso que registra una obligación financiera recurrente.
 * <p>
 * No crea {@code FinancialTransaction} ni pagos. Solo persiste el compromiso esperado.
 */
public class CreateRecurringFinancialObligationUseCase {

    private final RecurringFinancialObligationRepository recurringFinancialObligationRepository;

    public CreateRecurringFinancialObligationUseCase(
            RecurringFinancialObligationRepository recurringFinancialObligationRepository
    ) {
        this.recurringFinancialObligationRepository = Objects.requireNonNull(
                recurringFinancialObligationRepository,
                "Recurring financial obligation repository must not be null"
        );
    }

    public CreateRecurringFinancialObligationResult execute(CreateRecurringFinancialObligationCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        RecurringFinancialObligation obligation = RecurringFinancialObligation.create(
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

        RecurringFinancialObligation saved = recurringFinancialObligationRepository.save(obligation);
        return RecurringFinancialObligationReadMapper.toCreateResult(saved);
    }

    private void validateCommand(CreateRecurringFinancialObligationCommand command) {
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
