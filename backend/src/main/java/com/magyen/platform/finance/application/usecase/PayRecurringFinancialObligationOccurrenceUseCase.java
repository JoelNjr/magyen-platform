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

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Paga una ocurrencia PENDING creando exactamente un {@link FinancialTransaction} EXPENSE.
 * <p>
 * Atomicidad: el movimiento del ledger y el estado PAID se persisten en la misma transacción.
 * El monto proviene del snapshot de la ocurrencia, no del valor actual de la obligación.
 * <p>
 * La fecha del gasto es la fecha real de pago. El período de la ocurrencia
 * ({@code dueDate}) no se modifica y no sustituye a esa fecha.
 */
public class PayRecurringFinancialObligationOccurrenceUseCase {

    private final RecurringFinancialObligationOccurrenceRepository occurrenceRepository;
    private final RecurringFinancialObligationRepository recurringFinancialObligationRepository;
    private final FinancialTransactionRepository financialTransactionRepository;
    private final Clock clock;

    public PayRecurringFinancialObligationOccurrenceUseCase(
            RecurringFinancialObligationOccurrenceRepository occurrenceRepository,
            RecurringFinancialObligationRepository recurringFinancialObligationRepository,
            FinancialTransactionRepository financialTransactionRepository,
            Clock clock
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
        this.clock = Objects.requireNonNull(clock, "Clock must not be null");
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

        PaymentStamp paymentStamp = resolvePaymentStamp(command);
        FinancialCategory category = obligation.getType().toExpenseCategory();
        String description = "Payment of recurring obligation: " + obligation.getName();
        String observation = command.observation() != null ? command.observation() : occurrence.getObservation();

        FinancialTransaction transaction = FinancialTransaction.create(
                FinancialTransactionType.EXPENSE,
                occurrence.getExpectedAmount(),
                paymentStamp.transactionDate(),
                category.name(),
                description,
                observation,
                FinancialTransactionSourceType.RECURRING_OBLIGATION,
                occurrence.getId()
        );

        FinancialTransaction savedTransaction = financialTransactionRepository.save(transaction);
        occurrence.markPaid(savedTransaction.getId(), paymentStamp.paidAt());
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

    /**
     * Una {@code paymentDate} informada manda sobre el reloj y sobre {@code paidAt}.
     * El reloj solo cubre a los llamadores que no envían fecha.
     */
    private PaymentStamp resolvePaymentStamp(PayRecurringFinancialObligationOccurrenceCommand command) {
        if (command.paymentDate() != null) {
            LocalDate paymentDate = command.paymentDate();
            return new PaymentStamp(paymentDate, paymentDate.atStartOfDay());
        }
        if (command.paidAt() != null) {
            return new PaymentStamp(command.paidAt().toLocalDate(), command.paidAt());
        }
        LocalDateTime now = LocalDateTime.now(clock);
        return new PaymentStamp(now.toLocalDate(), now);
    }

    private record PaymentStamp(LocalDate transactionDate, LocalDateTime paidAt) {
    }
}
