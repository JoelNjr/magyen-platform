package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.PayRecurringFinancialObligationOccurrenceCommand;
import com.magyen.platform.finance.application.dto.PayRecurringFinancialObligationOccurrenceResult;
import com.magyen.platform.finance.domain.FinancialCategory;
import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.finance.domain.RecurringFinancialObligation;
import com.magyen.platform.finance.domain.RecurringFinancialObligationOccurrence;
import com.magyen.platform.finance.domain.RecurringFinancialObligationOccurrenceRepository;
import com.magyen.platform.finance.domain.RecurringFinancialObligationRepository;
import com.magyen.platform.finance.domain.RecurringObligationOccurrenceStatus;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;
import com.magyen.platform.finance.domain.exception.RecurringObligationOccurrenceAlreadyPaidException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Paga una ocurrencia PENDING creando exactamente un {@link FinancialTransaction} EXPENSE.
 * <p>
 * Atomicidad: el movimiento del ledger y el estado PAID se persisten en la misma transacción.
 * El monto proviene del snapshot de la ocurrencia, no del valor actual de la obligación.
 */
public class PayRecurringFinancialObligationOccurrenceUseCase {

    private final RecurringFinancialObligationOccurrenceRepository occurrenceRepository;
    private final RecurringFinancialObligationRepository recurringFinancialObligationRepository;
    private final FinancialTransactionRepository financialTransactionRepository;

    public PayRecurringFinancialObligationOccurrenceUseCase(
            RecurringFinancialObligationOccurrenceRepository occurrenceRepository,
            RecurringFinancialObligationRepository recurringFinancialObligationRepository,
            FinancialTransactionRepository financialTransactionRepository
    ) {
        this.occurrenceRepository = Objects.requireNonNull(
                occurrenceRepository,
                "Occurrence repository must not be null"
        );
        this.recurringFinancialObligationRepository = Objects.requireNonNull(
                recurringFinancialObligationRepository,
                "Recurring financial obligation repository must not be null"
        );
        this.financialTransactionRepository = Objects.requireNonNull(
                financialTransactionRepository,
                "Financial transaction repository must not be null"
        );
    }

    @Transactional
    public PayRecurringFinancialObligationOccurrenceResult execute(
            PayRecurringFinancialObligationOccurrenceCommand command
    ) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.occurrenceId(), "Occurrence id must not be null");

        RecurringFinancialObligationOccurrence occurrence = occurrenceRepository
                .findById(command.occurrenceId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Recurring financial obligation occurrence not found: " + command.occurrenceId()
                ));

        if (occurrence.getStatus() == RecurringObligationOccurrenceStatus.PAID) {
            throw new RecurringObligationOccurrenceAlreadyPaidException();
        }
        if (occurrence.getStatus() != RecurringObligationOccurrenceStatus.PENDING) {
            throw new FinanceDomainException(
                    "Only PENDING occurrences can be paid. Current status: " + occurrence.getStatus()
            );
        }

        RecurringFinancialObligation obligation = recurringFinancialObligationRepository
                .findById(occurrence.getRecurringObligationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Recurring financial obligation not found: " + occurrence.getRecurringObligationId()
                ));

        LocalDateTime paidAt = command.paidAt() == null ? LocalDateTime.now() : command.paidAt();
        FinancialCategory category = obligation.getType().toExpenseCategory();
        String description = "Payment of recurring obligation: " + obligation.getName();
        String observation = command.observation() != null ? command.observation() : occurrence.getObservation();

        FinancialTransaction transaction = FinancialTransaction.create(
                FinancialTransactionType.EXPENSE,
                occurrence.getExpectedAmount(),
                paidAt.toLocalDate(),
                category.name(),
                description,
                observation,
                FinancialTransactionSourceType.RECURRING_OBLIGATION,
                occurrence.getId()
        );

        FinancialTransaction savedTransaction = financialTransactionRepository.save(transaction);
        occurrence.markPaid(savedTransaction.getId(), paidAt);
        RecurringFinancialObligationOccurrence savedOccurrence = occurrenceRepository.save(occurrence);

        return new PayRecurringFinancialObligationOccurrenceResult(
                savedOccurrence.getId(),
                savedOccurrence.getRecurringObligationId(),
                savedOccurrence.getDueDate(),
                savedOccurrence.getExpectedAmount().getValue(),
                savedOccurrence.getStatus(),
                savedOccurrence.getPaidDate(),
                savedOccurrence.getFinancialTransactionId(),
                savedTransaction.getAmount().getValue(),
                savedTransaction.getCategory()
        );
    }
}
