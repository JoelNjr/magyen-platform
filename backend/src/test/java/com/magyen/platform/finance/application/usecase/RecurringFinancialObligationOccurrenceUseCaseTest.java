package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.CancelRecurringFinancialObligationOccurrenceCommand;
import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationCommand;
import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationOccurrenceCommand;
import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationOccurrenceResult;
import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationResult;
import com.magyen.platform.finance.application.dto.DeactivateRecurringFinancialObligationCommand;
import com.magyen.platform.finance.application.dto.PayRecurringFinancialObligationOccurrenceCommand;
import com.magyen.platform.finance.application.dto.PayRecurringFinancialObligationOccurrenceResult;
import com.magyen.platform.finance.application.dto.UpdateRecurringFinancialObligationCommand;
import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.finance.domain.RecurringFinancialObligationOccurrence;
import com.magyen.platform.finance.domain.RecurringFinancialObligationOccurrenceRepository;
import com.magyen.platform.finance.domain.RecurringFinancialObligationRepository;
import com.magyen.platform.finance.domain.RecurringObligationFrequency;
import com.magyen.platform.finance.domain.RecurringObligationOccurrenceStatus;
import com.magyen.platform.finance.domain.RecurringObligationType;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;
import com.magyen.platform.finance.domain.exception.RecurringObligationOccurrenceAlreadyExistsException;
import com.magyen.platform.finance.domain.exception.RecurringObligationOccurrenceAlreadyPaidException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class RecurringFinancialObligationOccurrenceUseCaseTest {

    @Autowired
    private CreateRecurringFinancialObligationUseCase createObligationUseCase;

    @Autowired
    private UpdateRecurringFinancialObligationUseCase updateObligationUseCase;

    @Autowired
    private DeactivateRecurringFinancialObligationUseCase deactivateObligationUseCase;

    @Autowired
    private CreateRecurringFinancialObligationOccurrenceUseCase createOccurrenceUseCase;

    @Autowired
    private PayRecurringFinancialObligationOccurrenceUseCase payOccurrenceUseCase;

    @Autowired
    private CancelRecurringFinancialObligationOccurrenceUseCase cancelOccurrenceUseCase;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    @Autowired
    private RecurringFinancialObligationOccurrenceRepository occurrenceRepository;

    @Autowired
    private RecurringFinancialObligationRepository recurringFinancialObligationRepository;

    private CreateRecurringFinancialObligationResult obligation;

    @BeforeEach
    void setUp() {
        obligation = createObligationUseCase.execute(
                new CreateRecurringFinancialObligationCommand(
                        "Internet",
                        RecurringObligationType.SERVICE,
                        new BigDecimal("120000.00"),
                        RecurringObligationFrequency.MONTHLY,
                        15,
                        LocalDate.of(2026, 8, 1),
                        null,
                        "Internet del taller",
                        null
                )
        );
    }

    @Test
    void createsOccurrenceWithoutFinancialTransaction() {
        long transactionsBefore = financialTransactionRepository.findAllNewestFirst().size();

        CreateRecurringFinancialObligationOccurrenceResult occurrence = createOccurrenceUseCase.execute(
                new CreateRecurringFinancialObligationOccurrenceCommand(
                        obligation.obligationId(),
                        LocalDate.of(2026, 8, 15),
                        null
                )
        );

        assertEquals(RecurringObligationOccurrenceStatus.PENDING, occurrence.status());
        assertEquals(new BigDecimal("120000.00"), occurrence.expectedAmount());
        assertEquals(transactionsBefore, financialTransactionRepository.findAllNewestFirst().size());
    }

    @Test
    void rejectsDuplicateOccurrence() {
        createOccurrenceUseCase.execute(
                new CreateRecurringFinancialObligationOccurrenceCommand(
                        obligation.obligationId(),
                        LocalDate.of(2026, 8, 15),
                        null
                )
        );

        assertThrows(
                RecurringObligationOccurrenceAlreadyExistsException.class,
                () -> createOccurrenceUseCase.execute(
                        new CreateRecurringFinancialObligationOccurrenceCommand(
                                obligation.obligationId(),
                                LocalDate.of(2026, 8, 15),
                                null
                        )
                )
        );
    }

    @Test
    void rejectsInactiveObligation() {
        deactivateObligationUseCase.execute(
                new DeactivateRecurringFinancialObligationCommand(obligation.obligationId())
        );

        assertThrows(
                FinanceDomainException.class,
                () -> createOccurrenceUseCase.execute(
                        new CreateRecurringFinancialObligationOccurrenceCommand(
                                obligation.obligationId(),
                                LocalDate.of(2026, 8, 15),
                                null
                        )
                )
        );
    }

    @Test
    void paysPendingOccurrenceCreatingExactlyOneTransactionFromSnapshot() {
        CreateRecurringFinancialObligationOccurrenceResult occurrence = createOccurrenceUseCase.execute(
                new CreateRecurringFinancialObligationOccurrenceCommand(
                        obligation.obligationId(),
                        LocalDate.of(2026, 8, 15),
                        null
                )
        );

        updateObligationUseCase.execute(
                new UpdateRecurringFinancialObligationCommand(
                        obligation.obligationId(),
                        "Internet",
                        RecurringObligationType.SERVICE,
                        new BigDecimal("150000.00"),
                        RecurringObligationFrequency.MONTHLY,
                        15,
                        LocalDate.of(2026, 8, 1),
                        null,
                        null,
                        null
                )
        );

        long transactionsBefore = financialTransactionRepository.findAllNewestFirst().size();

        PayRecurringFinancialObligationOccurrenceResult paid = payOccurrenceUseCase.execute(
                new PayRecurringFinancialObligationOccurrenceCommand(occurrence.occurrenceId())
        );

        assertEquals(RecurringObligationOccurrenceStatus.PAID, paid.status());
        assertEquals(new BigDecimal("120000.00"), paid.expectedAmount());
        assertEquals(new BigDecimal("120000.00"), paid.transactionAmount());
        assertEquals("SERVICES", paid.transactionCategory());
        assertEquals(transactionsBefore + 1, financialTransactionRepository.findAllNewestFirst().size());

        FinancialTransaction transaction = financialTransactionRepository
                .findById(paid.financialTransactionId())
                .orElseThrow();
        assertEquals(FinancialTransactionType.EXPENSE, transaction.getType());
        assertEquals(FinancialTransactionSourceType.RECURRING_OBLIGATION, transaction.getSourceType());
        assertEquals(occurrence.occurrenceId(), transaction.getSourceId());
        assertEquals(new BigDecimal("120000.00"), transaction.getAmount().getValue());
    }

    @Test
    void payAlreadyPaidReturnsConflictAndCreatesNoAdditionalTransaction() {
        CreateRecurringFinancialObligationOccurrenceResult occurrence = createOccurrenceUseCase.execute(
                new CreateRecurringFinancialObligationOccurrenceCommand(
                        obligation.obligationId(),
                        LocalDate.of(2026, 8, 15),
                        null
                )
        );
        payOccurrenceUseCase.execute(new PayRecurringFinancialObligationOccurrenceCommand(occurrence.occurrenceId()));
        long transactionsAfterPay = financialTransactionRepository.findAllNewestFirst().size();

        assertThrows(
                RecurringObligationOccurrenceAlreadyPaidException.class,
                () -> payOccurrenceUseCase.execute(
                        new PayRecurringFinancialObligationOccurrenceCommand(occurrence.occurrenceId())
                )
        );
        assertEquals(transactionsAfterPay, financialTransactionRepository.findAllNewestFirst().size());
    }

    @Test
    void cancelPendingCreatesNoTransaction() {
        CreateRecurringFinancialObligationOccurrenceResult occurrence = createOccurrenceUseCase.execute(
                new CreateRecurringFinancialObligationOccurrenceCommand(
                        obligation.obligationId(),
                        LocalDate.of(2026, 8, 15),
                        null
                )
        );
        long transactionsBefore = financialTransactionRepository.findAllNewestFirst().size();

        var cancelled = cancelOccurrenceUseCase.execute(
                new CancelRecurringFinancialObligationOccurrenceCommand(occurrence.occurrenceId())
        );

        assertEquals(RecurringObligationOccurrenceStatus.CANCELLED, cancelled.status());
        assertEquals(transactionsBefore, financialTransactionRepository.findAllNewestFirst().size());
    }

    @Test
    void cancelPaidAndCancelCancelledAreRejected() {
        CreateRecurringFinancialObligationOccurrenceResult occurrence = createOccurrenceUseCase.execute(
                new CreateRecurringFinancialObligationOccurrenceCommand(
                        obligation.obligationId(),
                        LocalDate.of(2026, 8, 15),
                        null
                )
        );
        payOccurrenceUseCase.execute(new PayRecurringFinancialObligationOccurrenceCommand(occurrence.occurrenceId()));

        assertThrows(
                FinanceDomainException.class,
                () -> cancelOccurrenceUseCase.execute(
                        new CancelRecurringFinancialObligationOccurrenceCommand(occurrence.occurrenceId())
                )
        );

        CreateRecurringFinancialObligationOccurrenceResult other = createOccurrenceUseCase.execute(
                new CreateRecurringFinancialObligationOccurrenceCommand(
                        obligation.obligationId(),
                        LocalDate.of(2026, 9, 15),
                        null
                )
        );
        cancelOccurrenceUseCase.execute(
                new CancelRecurringFinancialObligationOccurrenceCommand(other.occurrenceId())
        );
        assertThrows(
                FinanceDomainException.class,
                () -> cancelOccurrenceUseCase.execute(
                        new CancelRecurringFinancialObligationOccurrenceCommand(other.occurrenceId())
                )
        );
        assertThrows(
                FinanceDomainException.class,
                () -> payOccurrenceUseCase.execute(
                        new PayRecurringFinancialObligationOccurrenceCommand(other.occurrenceId())
                )
        );
    }

    @Test
    void paymentAtomicityLeavesNoPaidOccurrenceWithoutTransaction() {
        CreateRecurringFinancialObligationOccurrenceResult occurrence = createOccurrenceUseCase.execute(
                new CreateRecurringFinancialObligationOccurrenceCommand(
                        obligation.obligationId(),
                        LocalDate.of(2026, 8, 15),
                        null
                )
        );

        PayRecurringFinancialObligationOccurrenceResult paid = payOccurrenceUseCase.execute(
                new PayRecurringFinancialObligationOccurrenceCommand(occurrence.occurrenceId())
        );

        RecurringFinancialObligationOccurrence reloaded =
                occurrenceRepository.findById(occurrence.occurrenceId()).orElseThrow();
        assertEquals(RecurringObligationOccurrenceStatus.PAID, reloaded.getStatus());
        assertTrue(financialTransactionRepository.findById(paid.financialTransactionId()).isPresent());

        List<FinancialTransaction> sourced = financialTransactionRepository.findAllNewestFirst().stream()
                .filter(transaction -> occurrence.occurrenceId().equals(transaction.getSourceId()))
                .toList();
        assertEquals(1, sourced.size());
    }

    @Test
    void paymentFailureBeforeOccurrenceMarkLeavesNoLedgerEntryAndKeepsPending() {
        CreateRecurringFinancialObligationOccurrenceResult occurrence = createOccurrenceUseCase.execute(
                new CreateRecurringFinancialObligationOccurrenceCommand(
                        obligation.obligationId(),
                        LocalDate.of(2026, 9, 15),
                        null
                )
        );

        FinancialTransactionRepository failingTransactionRepository = new FinancialTransactionRepository() {
            @Override
            public FinancialTransaction save(FinancialTransaction financialTransaction) {
                throw new IllegalStateException("simulated ledger failure");
            }

            @Override
            public java.util.Optional<FinancialTransaction> findById(UUID id) {
                return java.util.Optional.empty();
            }

            @Override
            public List<FinancialTransaction> findAllNewestFirst() {
                return List.of();
            }

            @Override
            public List<FinancialTransaction> findByTransactionDateBetweenNewestFirst(
                    LocalDate fromDate,
                    LocalDate toDate
            ) {
                return List.of();
            }

            @Override
            public java.util.Optional<FinancialTransaction> findBySourceTypeAndSourceId(
                    FinancialTransactionSourceType sourceType,
                    UUID sourceId
            ) {
                return java.util.Optional.empty();
            }

            @Override
            public BigDecimal sumAmountByTypeBetween(
                    FinancialTransactionType type,
                    LocalDate fromDate,
                    LocalDate toDate
            ) {
                return BigDecimal.ZERO.setScale(2);
            }

            @Override
            public long countByTransactionDateBetween(LocalDate fromDate, LocalDate toDate) {
                return 0L;
            }
        };

        PayRecurringFinancialObligationOccurrenceUseCase payWithFailingLedger =
                new PayRecurringFinancialObligationOccurrenceUseCase(
                        occurrenceRepository,
                        recurringFinancialObligationRepository,
                        failingTransactionRepository
                );

        assertThrows(
                IllegalStateException.class,
                () -> payWithFailingLedger.execute(
                        new PayRecurringFinancialObligationOccurrenceCommand(occurrence.occurrenceId())
                )
        );

        RecurringFinancialObligationOccurrence stillPending =
                occurrenceRepository.findById(occurrence.occurrenceId()).orElseThrow();
        assertEquals(RecurringObligationOccurrenceStatus.PENDING, stillPending.getStatus());
        assertEquals(
                0,
                financialTransactionRepository.findAllNewestFirst().stream()
                        .filter(transaction -> occurrence.occurrenceId().equals(transaction.getSourceId()))
                        .count()
        );
    }
}
