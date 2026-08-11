package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationOccurrenceCommand;
import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationOccurrenceResult;
import com.magyen.platform.finance.domain.RecurringFinancialObligation;
import com.magyen.platform.finance.domain.RecurringFinancialObligationOccurrence;
import com.magyen.platform.finance.domain.RecurringFinancialObligationOccurrenceRepository;
import com.magyen.platform.finance.domain.RecurringFinancialObligationRepository;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;
import com.magyen.platform.finance.domain.exception.RecurringObligationOccurrenceAlreadyExistsException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Crea una ocurrencia PENDING a partir de una obligación activa.
 * <p>
 * No genera {@code FinancialTransaction}. El monto se congela como snapshot.
 */
public class CreateRecurringFinancialObligationOccurrenceUseCase {

    private final RecurringFinancialObligationRepository recurringFinancialObligationRepository;
    private final RecurringFinancialObligationOccurrenceRepository occurrenceRepository;

    public CreateRecurringFinancialObligationOccurrenceUseCase(
            RecurringFinancialObligationRepository recurringFinancialObligationRepository,
            RecurringFinancialObligationOccurrenceRepository occurrenceRepository
    ) {
        this.recurringFinancialObligationRepository = Objects.requireNonNull(
                recurringFinancialObligationRepository,
                "Recurring financial obligation repository must not be null"
        );
        this.occurrenceRepository = Objects.requireNonNull(
                occurrenceRepository,
                "Occurrence repository must not be null"
        );
    }

    @Transactional
    public CreateRecurringFinancialObligationOccurrenceResult execute(
            CreateRecurringFinancialObligationOccurrenceCommand command
    ) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        RecurringFinancialObligation obligation = recurringFinancialObligationRepository
                .findById(command.recurringObligationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Recurring financial obligation not found: " + command.recurringObligationId()
                ));

        if (!obligation.isActive()) {
            throw new FinanceDomainException(
                    "Cannot create occurrence for inactive recurring obligation: " + obligation.getId()
            );
        }

        obligation.requireCompatibleOccurrenceDueDate(command.dueDate());

        occurrenceRepository
                .findByRecurringObligationIdAndDueDate(obligation.getId(), command.dueDate())
                .ifPresent(existing -> {
                    throw new RecurringObligationOccurrenceAlreadyExistsException(
                            "An occurrence already exists for recurring obligation "
                                    + obligation.getId() + " and due date " + command.dueDate()
                    );
                });

        RecurringFinancialObligationOccurrence occurrence =
                RecurringFinancialObligationOccurrence.createPending(
                        obligation.getId(),
                        command.dueDate(),
                        obligation.getExpectedAmount(),
                        command.observation()
                );

        try {
            RecurringFinancialObligationOccurrence saved = occurrenceRepository.save(occurrence);
            return RecurringFinancialObligationOccurrenceReadMapper.toCreateResult(saved);
        } catch (DataIntegrityViolationException exception) {
            throw new RecurringObligationOccurrenceAlreadyExistsException(
                    "An occurrence already exists for recurring obligation "
                            + obligation.getId() + " and due date " + command.dueDate()
            );
        }
    }

    private void validateCommand(CreateRecurringFinancialObligationOccurrenceCommand command) {
        if (command.recurringObligationId() == null) {
            throw new FinanceDomainException("Recurring obligation id must not be null");
        }
        if (command.dueDate() == null) {
            throw new FinanceDomainException("Due date must not be null");
        }
    }
}
