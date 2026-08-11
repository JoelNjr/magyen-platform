package com.magyen.platform.finance.application.usecase;

import com.magyen.platform.finance.application.dto.CancelRecurringFinancialObligationOccurrenceCommand;
import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationCommand;
import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationOccurrenceCommand;
import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationOccurrenceResult;
import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationResult;
import com.magyen.platform.finance.application.dto.GetFinancialPeriodSummaryQuery;
import com.magyen.platform.finance.application.dto.GetFinancialPeriodSummaryResult;
import com.magyen.platform.finance.application.dto.GetOverdueFinancialObligationOccurrencesQuery;
import com.magyen.platform.finance.application.dto.GetOverdueFinancialObligationOccurrencesResult;
import com.magyen.platform.finance.application.dto.GetPendingFinancialObligationOccurrencesQuery;
import com.magyen.platform.finance.application.dto.GetPendingFinancialObligationOccurrencesResult;
import com.magyen.platform.finance.application.dto.GetUpcomingFinancialObligationOccurrencesQuery;
import com.magyen.platform.finance.application.dto.GetUpcomingFinancialObligationOccurrencesResult;
import com.magyen.platform.finance.application.dto.PayRecurringFinancialObligationOccurrenceCommand;
import com.magyen.platform.finance.application.dto.RegisterFinancialTransactionCommand;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.finance.domain.RecurringFinancialObligationOccurrenceRepository;
import com.magyen.platform.finance.domain.RecurringObligationFrequency;
import com.magyen.platform.finance.domain.RecurringObligationOccurrenceStatus;
import com.magyen.platform.finance.domain.RecurringObligationType;
import com.magyen.platform.finance.domain.exception.FinanceDomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class FinanceReadModelsUseCaseTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 8, 10);

    @MockitoBean
    private Clock clock;

    @Autowired
    private CreateRecurringFinancialObligationUseCase createObligationUseCase;

    @Autowired
    private CreateRecurringFinancialObligationOccurrenceUseCase createOccurrenceUseCase;

    @Autowired
    private PayRecurringFinancialObligationOccurrenceUseCase payOccurrenceUseCase;

    @Autowired
    private CancelRecurringFinancialObligationOccurrenceUseCase cancelOccurrenceUseCase;

    @Autowired
    private RegisterFinancialTransactionUseCase registerFinancialTransactionUseCase;

    @Autowired
    private GetPendingFinancialObligationOccurrencesUseCase getPendingUseCase;

    @Autowired
    private GetOverdueFinancialObligationOccurrencesUseCase getOverdueUseCase;

    @Autowired
    private GetUpcomingFinancialObligationOccurrencesUseCase getUpcomingUseCase;

    @Autowired
    private GetFinancialPeriodSummaryUseCase getPeriodSummaryUseCase;

    @Autowired
    private RecurringFinancialObligationOccurrenceRepository occurrenceRepository;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    @BeforeEach
    void setFixedClock() {
        ZoneId zone = ZoneId.systemDefault();
        when(clock.getZone()).thenReturn(zone);
        when(clock.instant()).thenReturn(TODAY.atStartOfDay(zone).toInstant());
    }

    @Test
    void pendingTotalsIncludeOverdueAndExcludePaidCancelled() {
        BigDecimal pendingBefore = occurrenceRepository.sumPendingExpectedAmount();
        BigDecimal overdueBefore = occurrenceRepository.sumPendingExpectedAmountDueBefore(TODAY);

        CreateRecurringFinancialObligationResult internet = createObligation(
                "Internet-" + suffix(),
                RecurringObligationType.SERVICE,
                "120000.00",
                9
        );
        CreateRecurringFinancialObligationResult rent = createObligation(
                "Arriendo-" + suffix(),
                RecurringObligationType.OTHER,
                "800000.00",
                15
        );
        CreateRecurringFinancialObligationResult payroll = createObligation(
                "Payroll-" + suffix(),
                RecurringObligationType.PAYROLL,
                "1500000.00",
                20
        );

        CreateRecurringFinancialObligationOccurrenceResult internetOccurrence = createOccurrence(
                internet.obligationId(),
                LocalDate.of(2026, 8, 9)
        );
        createOccurrence(rent.obligationId(), LocalDate.of(2026, 8, 15));
        createOccurrence(payroll.obligationId(), LocalDate.of(2026, 8, 20));

        Set<UUID> owned = Set.of(
                internet.obligationId(),
                rent.obligationId(),
                payroll.obligationId()
        );

        GetPendingFinancialObligationOccurrencesResult pending = getPendingUseCase.execute(
                GetPendingFinancialObligationOccurrencesQuery.create()
        );
        assertEquals(
                0,
                pending.totalPendingAmount().subtract(pendingBefore)
                        .compareTo(new BigDecimal("2420000.00"))
        );
        assertEquals(3, countOwned(pending, owned));
        assertTrue(pending.occurrences().stream()
                .filter(item -> owned.contains(item.recurringObligationId()))
                .allMatch(item -> item.status() == RecurringObligationOccurrenceStatus.PENDING));

        GetOverdueFinancialObligationOccurrencesResult overdue = getOverdueUseCase.execute(
                GetOverdueFinancialObligationOccurrencesQuery.create()
        );
        assertEquals(
                0,
                overdue.totalOverdueAmount().subtract(overdueBefore)
                        .compareTo(new BigDecimal("120000.00"))
        );
        assertEquals(1, countOwned(overdue.occurrences(), owned));
        assertTrue(overdue.occurrences().stream()
                .filter(item -> item.occurrenceId().equals(internetOccurrence.occurrenceId()))
                .allMatch(item -> item.overdue() && item.daysOverdue() == 1));

        GetUpcomingFinancialObligationOccurrencesResult upcoming = getUpcomingUseCase.execute(
                new GetUpcomingFinancialObligationOccurrencesQuery(7)
        );
        Set<UUID> upcomingOwned = upcoming.occurrences().stream()
                .filter(item -> owned.contains(item.recurringObligationId()))
                .map(item -> item.occurrenceId())
                .collect(Collectors.toSet());
        // Aug 10 + 7 days → Aug 17: rent (15) included; payroll (20) excluded; overdue excluded.
        assertEquals(1, upcomingOwned.size());
        assertFalse(upcomingOwned.contains(internetOccurrence.occurrenceId()));

        GetUpcomingFinancialObligationOccurrencesResult upcomingWider = getUpcomingUseCase.execute(
                new GetUpcomingFinancialObligationOccurrencesQuery(14)
        );
        assertEquals(2, upcomingWider.occurrences().stream()
                .filter(item -> owned.contains(item.recurringObligationId()))
                .count());

        payOccurrenceUseCase.execute(
                new PayRecurringFinancialObligationOccurrenceCommand(
                        internetOccurrence.occurrenceId(),
                        LocalDateTime.of(2026, 8, 10, 10, 0),
                        null
                )
        );

        GetPendingFinancialObligationOccurrencesResult pendingAfterPay = getPendingUseCase.execute(
                GetPendingFinancialObligationOccurrencesQuery.create()
        );
        assertEquals(
                0,
                pendingAfterPay.totalPendingAmount().subtract(pendingBefore)
                        .compareTo(new BigDecimal("2300000.00"))
        );
        assertEquals(2, countOwned(pendingAfterPay, owned));

        GetOverdueFinancialObligationOccurrencesResult overdueAfterPay = getOverdueUseCase.execute(
                GetOverdueFinancialObligationOccurrencesQuery.create()
        );
        assertEquals(
                0,
                overdueAfterPay.totalOverdueAmount().subtract(overdueBefore).compareTo(BigDecimal.ZERO.setScale(2))
        );

        CreateRecurringFinancialObligationOccurrenceResult cancelTarget = createOccurrence(
                rent.obligationId(),
                LocalDate.of(2026, 9, 15)
        );
        BigDecimal pendingBeforeCancel = occurrenceRepository.sumPendingExpectedAmount();
        cancelOccurrenceUseCase.execute(
                new CancelRecurringFinancialObligationOccurrenceCommand(cancelTarget.occurrenceId())
        );
        GetPendingFinancialObligationOccurrencesResult pendingAfterCancel = getPendingUseCase.execute(
                GetPendingFinancialObligationOccurrencesQuery.create()
        );
        assertEquals(
                0,
                pendingBeforeCancel.subtract(pendingAfterCancel.totalPendingAmount())
                        .compareTo(new BigDecimal("800000.00"))
        );
    }

    @Test
    void pendingCommitmentsDoNotAffectLedgerSummaryAndPayDoes() {
        CreateRecurringFinancialObligationResult obligation = createObligation(
                "Servicio-" + suffix(),
                RecurringObligationType.SERVICE,
                "100000.00",
                15
        );
        CreateRecurringFinancialObligationOccurrenceResult occurrence = createOccurrence(
                obligation.obligationId(),
                LocalDate.of(2026, 8, 15)
        );

        BigDecimal expenseBefore = financialTransactionRepository.sumAmountByTypeBetween(
                FinancialTransactionType.EXPENSE,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31)
        );

        GetPendingFinancialObligationOccurrencesResult pending = getPendingUseCase.execute(
                GetPendingFinancialObligationOccurrencesQuery.create()
        );
        assertTrue(pending.occurrences().stream()
                .anyMatch(item -> item.occurrenceId().equals(occurrence.occurrenceId())));

        GetFinancialPeriodSummaryResult summaryBeforePay = getPeriodSummaryUseCase.execute(
                new GetFinancialPeriodSummaryQuery(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31))
        );
        assertEquals(expenseBefore, summaryBeforePay.totalExpense());

        payOccurrenceUseCase.execute(
                new PayRecurringFinancialObligationOccurrenceCommand(
                        occurrence.occurrenceId(),
                        LocalDateTime.of(2026, 8, 16, 9, 0),
                        null
                )
        );

        GetPendingFinancialObligationOccurrencesResult pendingAfterPay = getPendingUseCase.execute(
                GetPendingFinancialObligationOccurrencesQuery.create()
        );
        assertTrue(pendingAfterPay.occurrences().stream()
                .noneMatch(item -> item.occurrenceId().equals(occurrence.occurrenceId())));

        GetFinancialPeriodSummaryResult summaryAfterPay = getPeriodSummaryUseCase.execute(
                new GetFinancialPeriodSummaryQuery(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31))
        );
        assertEquals(
                0,
                summaryAfterPay.totalExpense().subtract(expenseBefore)
                        .compareTo(new BigDecimal("100000.00"))
        );
    }

    @Test
    void periodSummaryAggregatesIncomeExpenseNetAndBoundaries() {
        BigDecimal incomeBefore = financialTransactionRepository.sumAmountByTypeBetween(
                FinancialTransactionType.INCOME,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31)
        );
        BigDecimal expenseBefore = financialTransactionRepository.sumAmountByTypeBetween(
                FinancialTransactionType.EXPENSE,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31)
        );
        long countBefore = financialTransactionRepository.countByTransactionDateBetween(
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31)
        );

        registerTransaction(FinancialTransactionType.INCOME, "5000000.00", LocalDate.of(2026, 8, 1));
        registerTransaction(FinancialTransactionType.EXPENSE, "2000000.00", LocalDate.of(2026, 8, 15));
        registerTransaction(FinancialTransactionType.EXPENSE, "1200000.00", LocalDate.of(2026, 8, 31));
        registerTransaction(FinancialTransactionType.INCOME, "999.00", LocalDate.of(2026, 7, 31));
        registerTransaction(FinancialTransactionType.EXPENSE, "888.00", LocalDate.of(2026, 9, 1));

        GetFinancialPeriodSummaryResult summary = getPeriodSummaryUseCase.execute(
                new GetFinancialPeriodSummaryQuery(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31))
        );

        assertEquals(
                0,
                summary.totalIncome().subtract(incomeBefore).compareTo(new BigDecimal("5000000.00"))
        );
        assertEquals(
                0,
                summary.totalExpense().subtract(expenseBefore).compareTo(new BigDecimal("3200000.00"))
        );
        assertEquals(
                0,
                summary.netResult()
                        .subtract(incomeBefore.subtract(expenseBefore))
                        .compareTo(new BigDecimal("1800000.00"))
        );
        assertEquals(countBefore + 3, summary.transactionCount());

        GetFinancialPeriodSummaryResult empty = getPeriodSummaryUseCase.execute(
                new GetFinancialPeriodSummaryQuery(LocalDate.of(2099, 1, 1), LocalDate.of(2099, 1, 31))
        );
        assertEquals(new BigDecimal("0.00"), empty.totalIncome());
        assertEquals(new BigDecimal("0.00"), empty.totalExpense());
        assertEquals(new BigDecimal("0.00"), empty.netResult());
        assertEquals(0L, empty.transactionCount());
    }

    @Test
    void upcomingAndSummaryValidationRejectInvalidInputs() {
        assertThrows(
                FinanceDomainException.class,
                () -> getUpcomingUseCase.execute(new GetUpcomingFinancialObligationOccurrencesQuery(-1))
        );
        assertThrows(
                FinanceDomainException.class,
                () -> getUpcomingUseCase.execute(new GetUpcomingFinancialObligationOccurrencesQuery(367))
        );
        assertThrows(
                FinanceDomainException.class,
                () -> getPeriodSummaryUseCase.execute(
                        new GetFinancialPeriodSummaryQuery(null, LocalDate.of(2026, 8, 31))
                )
        );
        assertThrows(
                FinanceDomainException.class,
                () -> getPeriodSummaryUseCase.execute(
                        new GetFinancialPeriodSummaryQuery(LocalDate.of(2026, 8, 31), LocalDate.of(2026, 8, 1))
                )
        );
    }

    @Test
    void dueTodayIsUpcomingNotOverdue() {
        CreateRecurringFinancialObligationResult obligation = createObligation(
                "Hoy-" + suffix(),
                RecurringObligationType.SERVICE,
                "50000.00",
                10
        );
        CreateRecurringFinancialObligationOccurrenceResult todayOccurrence =
                createOccurrence(obligation.obligationId(), TODAY);

        GetOverdueFinancialObligationOccurrencesResult overdue = getOverdueUseCase.execute(
                GetOverdueFinancialObligationOccurrencesQuery.create()
        );
        assertTrue(overdue.occurrences().stream()
                .noneMatch(item -> item.occurrenceId().equals(todayOccurrence.occurrenceId())));

        GetUpcomingFinancialObligationOccurrencesResult upcoming = getUpcomingUseCase.execute(
                new GetUpcomingFinancialObligationOccurrencesQuery(0)
        );
        assertTrue(upcoming.occurrences().stream()
                .anyMatch(item -> item.occurrenceId().equals(todayOccurrence.occurrenceId())
                        && !item.overdue()
                        && item.daysUntilDue() == 0));
    }

    private CreateRecurringFinancialObligationResult createObligation(
            String name,
            RecurringObligationType type,
            String amount,
            int dueDay
    ) {
        return createObligationUseCase.execute(
                new CreateRecurringFinancialObligationCommand(
                        name,
                        type,
                        new BigDecimal(amount),
                        RecurringObligationFrequency.MONTHLY,
                        dueDay,
                        LocalDate.of(2026, 8, 1),
                        null,
                        null,
                        null
                )
        );
    }

    private CreateRecurringFinancialObligationOccurrenceResult createOccurrence(
            UUID obligationId,
            LocalDate dueDate
    ) {
        return createOccurrenceUseCase.execute(
                new CreateRecurringFinancialObligationOccurrenceCommand(obligationId, dueDate, null)
        );
    }

    private void registerTransaction(FinancialTransactionType type, String amount, LocalDate date) {
        registerFinancialTransactionUseCase.execute(
                new RegisterFinancialTransactionCommand(
                        type,
                        new BigDecimal(amount),
                        date,
                        "TESTS",
                        "Finance read model test",
                        null,
                        null,
                        null
                )
        );
    }

    private static int countOwned(
            GetPendingFinancialObligationOccurrencesResult result,
            Set<UUID> ownedObligationIds
    ) {
        return countOwned(result.occurrences(), ownedObligationIds);
    }

    private static int countOwned(
            java.util.List<com.magyen.platform.finance.application.dto.FinancialObligationOccurrenceCommitmentResult> items,
            Set<UUID> ownedObligationIds
    ) {
        return (int) items.stream()
                .filter(item -> ownedObligationIds.contains(item.recurringObligationId()))
                .count();
    }

    private static String suffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
