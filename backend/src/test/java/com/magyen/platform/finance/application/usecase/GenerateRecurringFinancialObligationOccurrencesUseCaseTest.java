package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationCommand;
import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationResult;
import com.magyen.platform.finance.application.dto.DeactivateRecurringFinancialObligationCommand;
import com.magyen.platform.finance.application.dto.GenerateRecurringFinancialObligationOccurrencesCommand;
import com.magyen.platform.finance.application.dto.GenerateRecurringFinancialObligationOccurrencesResult;
import com.magyen.platform.finance.application.dto.PayRecurringFinancialObligationOccurrenceCommand;
import com.magyen.platform.finance.application.dto.UpdateRecurringFinancialObligationCommand;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.RecurringFinancialObligationOccurrence;
import com.magyen.platform.finance.domain.RecurringFinancialObligationOccurrenceRepository;
import com.magyen.platform.finance.domain.RecurringObligationFrequency;
import com.magyen.platform.finance.domain.RecurringObligationOccurrenceStatus;
import com.magyen.platform.finance.domain.RecurringObligationType;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class GenerateRecurringFinancialObligationOccurrencesUseCaseTest {

    @Autowired
    private CreateRecurringFinancialObligationUseCase createObligationUseCase;

    @Autowired
    private UpdateRecurringFinancialObligationUseCase updateObligationUseCase;

    @Autowired
    private DeactivateRecurringFinancialObligationUseCase deactivateObligationUseCase;

    @Autowired
    private GenerateRecurringFinancialObligationOccurrencesUseCase generateOccurrencesUseCase;

    @Autowired
    private PayRecurringFinancialObligationOccurrenceUseCase payOccurrenceUseCase;

    @Autowired
    private RecurringFinancialObligationOccurrenceRepository occurrenceRepository;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    @Test
    void generatesIdempotentlyWithSnapshotsAndNoTransactions() {
        CreateRecurringFinancialObligationResult internet = createObligation(
                "Internet-" + UUID.randomUUID().toString().substring(0, 8),
                RecurringObligationType.SERVICE,
                "120000.00",
                15,
                LocalDate.of(2026, 8, 1),
                null
        );
        CreateRecurringFinancialObligationResult rent = createObligation(
                "Arriendo-" + UUID.randomUUID().toString().substring(0, 8),
                RecurringObligationType.OTHER,
                "800000.00",
                5,
                LocalDate.of(2026, 8, 1),
                null
        );
        Set<UUID> owned = Set.of(internet.obligationId(), rent.obligationId());

        long transactionsBefore = financialTransactionRepository.findAllNewestFirst().size();

        GenerateRecurringFinancialObligationOccurrencesResult august =
                generateOccurrencesUseCase.execute(
                        new GenerateRecurringFinancialObligationOccurrencesCommand(
                                LocalDate.of(2026, 8, 1),
                                LocalDate.of(2026, 8, 31)
                        )
                );

        assertEquals(2, countCreatedFor(august, owned));
        assertTrue(august.createdOccurrences().stream()
                .filter(item -> owned.contains(item.recurringObligationId()))
                .allMatch(item -> item.status() == RecurringObligationOccurrenceStatus.PENDING));
        assertEquals(transactionsBefore, financialTransactionRepository.findAllNewestFirst().size());

        GenerateRecurringFinancialObligationOccurrencesResult augustAgain =
                generateOccurrencesUseCase.execute(
                        new GenerateRecurringFinancialObligationOccurrencesCommand(
                                LocalDate.of(2026, 8, 1),
                                LocalDate.of(2026, 8, 31)
                        )
                );
        assertEquals(0, countCreatedFor(augustAgain, owned));
        assertTrue(augustAgain.occurrencesAlreadyExisting() >= 2);

        updateObligationUseCase.execute(
                new UpdateRecurringFinancialObligationCommand(
                        internet.obligationId(),
                        internet.name(),
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

        GenerateRecurringFinancialObligationOccurrencesResult augustSeptember =
                generateOccurrencesUseCase.execute(
                        new GenerateRecurringFinancialObligationOccurrencesCommand(
                                LocalDate.of(2026, 8, 1),
                                LocalDate.of(2026, 9, 30)
                        )
                );

        assertEquals(2, countCreatedFor(augustSeptember, owned));

        RecurringFinancialObligationOccurrence augustInternet = occurrenceRepository
                .findByRecurringObligationIdAndDueDate(
                        internet.obligationId(),
                        LocalDate.of(2026, 8, 15)
                )
                .orElseThrow();
        RecurringFinancialObligationOccurrence septemberInternet = occurrenceRepository
                .findByRecurringObligationIdAndDueDate(
                        internet.obligationId(),
                        LocalDate.of(2026, 9, 15)
                )
                .orElseThrow();

        assertEquals(new BigDecimal("120000.00"), augustInternet.getExpectedAmount().getValue());
        assertEquals(new BigDecimal("150000.00"), septemberInternet.getExpectedAmount().getValue());
        assertEquals(transactionsBefore, financialTransactionRepository.findAllNewestFirst().size());
    }

    @Test
    void skipsInactiveObligationsAndKeepsExistingOccurrences() {
        CreateRecurringFinancialObligationResult obligation = createObligation(
                "Internet-" + UUID.randomUUID().toString().substring(0, 8),
                RecurringObligationType.SERVICE,
                "120000.00",
                15,
                LocalDate.of(2026, 8, 1),
                null
        );

        generateOccurrencesUseCase.execute(
                new GenerateRecurringFinancialObligationOccurrencesCommand(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                )
        );

        deactivateObligationUseCase.execute(
                new DeactivateRecurringFinancialObligationCommand(obligation.obligationId())
        );

        GenerateRecurringFinancialObligationOccurrencesResult second =
                generateOccurrencesUseCase.execute(
                        new GenerateRecurringFinancialObligationOccurrencesCommand(
                                LocalDate.of(2026, 8, 1),
                                LocalDate.of(2026, 9, 30)
                        )
                );

        assertEquals(0, countCreatedFor(second, Set.of(obligation.obligationId())));
        assertTrue(second.occurrencesSkippedInactive() >= 1);
        assertTrue(occurrenceRepository
                .findByRecurringObligationIdAndDueDate(
                        obligation.obligationId(),
                        LocalDate.of(2026, 8, 15)
                )
                .isPresent());
        assertTrue(occurrenceRepository
                .findByRecurringObligationIdAndDueDate(
                        obligation.obligationId(),
                        LocalDate.of(2026, 9, 15)
                )
                .isEmpty());
    }

    @Test
    void respectsStartAndEndDateBoundaries() {
        CreateRecurringFinancialObligationResult obligation = createObligation(
                "Internet-" + UUID.randomUUID().toString().substring(0, 8),
                RecurringObligationType.SERVICE,
                "120000.00",
                15,
                LocalDate.of(2026, 8, 15),
                LocalDate.of(2026, 9, 15)
        );

        GenerateRecurringFinancialObligationOccurrencesResult result =
                generateOccurrencesUseCase.execute(
                        new GenerateRecurringFinancialObligationOccurrencesCommand(
                                LocalDate.of(2026, 7, 1),
                                LocalDate.of(2026, 12, 31)
                        )
                );

        assertEquals(2, countCreatedFor(result, Set.of(obligation.obligationId())));
        assertTrue(occurrenceRepository
                .findByRecurringObligationIdAndDueDate(
                        obligation.obligationId(),
                        LocalDate.of(2026, 8, 15)
                )
                .isPresent());
        assertTrue(occurrenceRepository
                .findByRecurringObligationIdAndDueDate(
                        obligation.obligationId(),
                        LocalDate.of(2026, 9, 15)
                )
                .isPresent());
        assertTrue(occurrenceRepository
                .findByRecurringObligationIdAndDueDate(
                        obligation.obligationId(),
                        LocalDate.of(2026, 7, 15)
                )
                .isEmpty());
        assertTrue(occurrenceRepository
                .findByRecurringObligationIdAndDueDate(
                        obligation.obligationId(),
                        LocalDate.of(2026, 10, 15)
                )
                .isEmpty());
    }

    @Test
    void generatedOccurrenceCanStillBePaid() {
        CreateRecurringFinancialObligationResult obligation = createObligation(
                "Internet-" + UUID.randomUUID().toString().substring(0, 8),
                RecurringObligationType.SERVICE,
                "120000.00",
                15,
                LocalDate.of(2026, 8, 1),
                null
        );

        generateOccurrencesUseCase.execute(
                new GenerateRecurringFinancialObligationOccurrencesCommand(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                )
        );

        RecurringFinancialObligationOccurrence occurrence = occurrenceRepository
                .findByRecurringObligationIdAndDueDate(
                        obligation.obligationId(),
                        LocalDate.of(2026, 8, 15)
                )
                .orElseThrow();

        long transactionsBefore = financialTransactionRepository.findAllNewestFirst().size();
        payOccurrenceUseCase.execute(
                new PayRecurringFinancialObligationOccurrenceCommand(occurrence.getId())
        );
        assertEquals(transactionsBefore + 1, financialTransactionRepository.findAllNewestFirst().size());
        assertEquals(
                RecurringObligationOccurrenceStatus.PAID,
                occurrenceRepository.findById(occurrence.getId()).orElseThrow().getStatus()
        );
    }

    @Test
    void rejectsInvalidRanges() {
        assertThrows(
                FinanceDomainException.class,
                () -> generateOccurrencesUseCase.execute(
                        new GenerateRecurringFinancialObligationOccurrencesCommand(
                                null,
                                LocalDate.of(2026, 8, 31)
                        )
                )
        );
        assertThrows(
                FinanceDomainException.class,
                () -> generateOccurrencesUseCase.execute(
                        new GenerateRecurringFinancialObligationOccurrencesCommand(
                                LocalDate.of(2026, 8, 31),
                                LocalDate.of(2026, 8, 1)
                        )
                )
        );
        assertThrows(
                FinanceDomainException.class,
                () -> generateOccurrencesUseCase.execute(
                        new GenerateRecurringFinancialObligationOccurrencesCommand(
                                LocalDate.of(2026, 1, 1),
                                LocalDate.of(2027, 1, 2)
                        )
                )
        );
    }

    private static long countCreatedFor(
            GenerateRecurringFinancialObligationOccurrencesResult result,
            Set<UUID> obligationIds
    ) {
        return result.createdOccurrences().stream()
                .filter(item -> obligationIds.contains(item.recurringObligationId()))
                .count();
    }

    private CreateRecurringFinancialObligationResult createObligation(
            String name,
            RecurringObligationType type,
            String amount,
            int dueDay,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return createObligationUseCase.execute(
                new CreateRecurringFinancialObligationCommand(
                        name,
                        type,
                        new BigDecimal(amount),
                        RecurringObligationFrequency.MONTHLY,
                        dueDay,
                        startDate,
                        endDate,
                        null,
                        null
                )
        );
    }
}
