package com.magyen.platform.finance.infrastructure.persistence;

import com.magyen.platform.finance.domain.FinancialAmount;
import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.finance.domain.RecurringFinancialObligation;
import com.magyen.platform.finance.domain.RecurringFinancialObligationOccurrence;
import com.magyen.platform.finance.domain.RecurringFinancialObligationOccurrenceRepository;
import com.magyen.platform.finance.domain.RecurringFinancialObligationRepository;
import com.magyen.platform.finance.domain.RecurringObligationFrequency;
import com.magyen.platform.finance.domain.RecurringObligationType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class FinanceReadQueryPersistenceTest {

    @Autowired
    private RecurringFinancialObligationRepository obligationRepository;

    @Autowired
    private RecurringFinancialObligationOccurrenceRepository occurrenceRepository;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void pendingQueriesFilterStatusOrderAndAggregate() {
        RecurringFinancialObligation first = saveObligation("A-" + suffix(), "100000.00", 5);
        RecurringFinancialObligation second = saveObligation("B-" + suffix(), "200000.00", 5);
        RecurringFinancialObligation third = saveObligation("C-" + suffix(), "50000.00", 15);

        RecurringFinancialObligationOccurrence olderSmall = occurrenceRepository.save(
                RecurringFinancialObligationOccurrence.createPending(
                        first.getId(),
                        LocalDate.of(2026, 8, 5),
                        FinancialAmount.of(new BigDecimal("100000.00")),
                        null
                )
        );
        RecurringFinancialObligationOccurrence olderLarge = occurrenceRepository.save(
                RecurringFinancialObligationOccurrence.createPending(
                        second.getId(),
                        LocalDate.of(2026, 8, 5),
                        FinancialAmount.of(new BigDecimal("200000.00")),
                        null
                )
        );
        RecurringFinancialObligationOccurrence later = occurrenceRepository.save(
                RecurringFinancialObligationOccurrence.createPending(
                        third.getId(),
                        LocalDate.of(2026, 8, 15),
                        FinancialAmount.of(new BigDecimal("50000.00")),
                        null
                )
        );
        RecurringFinancialObligationOccurrence paid = occurrenceRepository.save(
                RecurringFinancialObligationOccurrence.createPending(
                        first.getId(),
                        LocalDate.of(2026, 8, 9),
                        FinancialAmount.of(new BigDecimal("100000.00")),
                        null
                )
        );
        paid.markPaid(UUID.randomUUID(), LocalDate.of(2026, 8, 9).atStartOfDay());
        occurrenceRepository.save(paid);

        entityManager.flush();
        entityManager.clear();

        LocalDate today = LocalDate.of(2026, 8, 10);
        List<RecurringFinancialObligationOccurrence> overdue =
                occurrenceRepository.findPendingDueBefore(today);
        List<UUID> overdueOwned = overdue.stream()
                .map(RecurringFinancialObligationOccurrence::getId)
                .filter(id -> id.equals(olderSmall.getId()) || id.equals(olderLarge.getId()) || id.equals(later.getId())
                        || id.equals(paid.getId()))
                .toList();
        assertEquals(List.of(olderLarge.getId(), olderSmall.getId()), overdueOwned);

        List<RecurringFinancialObligationOccurrence> upcoming =
                occurrenceRepository.findPendingDueBetween(today, today.plusDays(7));
        assertTrue(upcoming.stream().anyMatch(item -> item.getId().equals(later.getId())));
        assertTrue(upcoming.stream().noneMatch(item -> item.getId().equals(olderSmall.getId())));
        assertTrue(upcoming.stream().noneMatch(item -> item.getId().equals(paid.getId())));

        BigDecimal pendingSum = occurrenceRepository.sumPendingExpectedAmount();
        BigDecimal overdueSum = occurrenceRepository.sumPendingExpectedAmountDueBefore(today);
        assertTrue(pendingSum.compareTo(new BigDecimal("350000.00")) >= 0);
        assertTrue(overdueSum.compareTo(new BigDecimal("300000.00")) >= 0);
    }

    @Test
    void ledgerAggregationsRespectTypeAndDateBoundaries() {
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

        financialTransactionRepository.save(
                FinancialTransaction.create(
                        FinancialTransactionType.INCOME,
                        FinancialAmount.of(new BigDecimal("1000.50")),
                        LocalDate.of(2026, 8, 1),
                        "SALES",
                        null,
                        null,
                        null,
                        null
                )
        );
        financialTransactionRepository.save(
                FinancialTransaction.create(
                        FinancialTransactionType.EXPENSE,
                        FinancialAmount.of(new BigDecimal("250.25")),
                        LocalDate.of(2026, 8, 31),
                        "SERVICES",
                        null,
                        null,
                        null,
                        null
                )
        );
        financialTransactionRepository.save(
                FinancialTransaction.create(
                        FinancialTransactionType.INCOME,
                        FinancialAmount.of(new BigDecimal("10.00")),
                        LocalDate.of(2026, 7, 31),
                        "SALES",
                        null,
                        null,
                        null,
                        null
                )
        );

        entityManager.flush();
        entityManager.clear();

        assertEquals(
                0,
                financialTransactionRepository
                        .sumAmountByTypeBetween(
                                FinancialTransactionType.INCOME,
                                LocalDate.of(2026, 8, 1),
                                LocalDate.of(2026, 8, 31)
                        )
                        .subtract(incomeBefore)
                        .compareTo(new BigDecimal("1000.50"))
        );
        assertEquals(
                0,
                financialTransactionRepository
                        .sumAmountByTypeBetween(
                                FinancialTransactionType.EXPENSE,
                                LocalDate.of(2026, 8, 1),
                                LocalDate.of(2026, 8, 31)
                        )
                        .subtract(expenseBefore)
                        .compareTo(new BigDecimal("250.25"))
        );
        assertEquals(
                countBefore + 2,
                financialTransactionRepository.countByTransactionDateBetween(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                )
        );
    }

    private RecurringFinancialObligation saveObligation(String name, String amount, int dueDay) {
        return obligationRepository.save(
                RecurringFinancialObligation.create(
                        name,
                        RecurringObligationType.SERVICE,
                        FinancialAmount.of(new BigDecimal(amount)),
                        RecurringObligationFrequency.MONTHLY,
                        dueDay,
                        LocalDate.of(2026, 8, 1),
                        null,
                        null,
                        null
                )
        );
    }

    private static String suffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
