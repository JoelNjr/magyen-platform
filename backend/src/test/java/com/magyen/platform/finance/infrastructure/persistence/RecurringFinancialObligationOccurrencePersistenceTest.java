package com.magyen.platform.finance.infrastructure.persistence;

import com.magyen.platform.finance.domain.FinancialAmount;
import com.magyen.platform.finance.domain.RecurringFinancialObligation;
import com.magyen.platform.finance.domain.RecurringFinancialObligationOccurrence;
import com.magyen.platform.finance.domain.RecurringFinancialObligationOccurrenceRepository;
import com.magyen.platform.finance.domain.RecurringFinancialObligationRepository;
import com.magyen.platform.finance.domain.RecurringObligationFrequency;
import com.magyen.platform.finance.domain.RecurringObligationOccurrenceStatus;
import com.magyen.platform.finance.domain.RecurringObligationType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class RecurringFinancialObligationOccurrencePersistenceTest {

    @Autowired
    private RecurringFinancialObligationRepository recurringFinancialObligationRepository;

    @Autowired
    private RecurringFinancialObligationOccurrenceRepository occurrenceRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsSnapshotStatusPaidFieldsAndUniqueConstraint() {
        RecurringFinancialObligation obligation = recurringFinancialObligationRepository.save(
                RecurringFinancialObligation.create(
                        "Internet",
                        RecurringObligationType.SERVICE,
                        FinancialAmount.of(new BigDecimal("120000.10")),
                        RecurringObligationFrequency.MONTHLY,
                        15,
                        LocalDate.of(2026, 8, 1),
                        null,
                        null,
                        null
                )
        );

        RecurringFinancialObligationOccurrence saved = occurrenceRepository.save(
                RecurringFinancialObligationOccurrence.createPending(
                        obligation.getId(),
                        LocalDate.of(2026, 8, 15),
                        FinancialAmount.of(new BigDecimal("120000.10")),
                        "Agosto"
                )
        );

        entityManager.flush();
        entityManager.clear();

        RecurringFinancialObligationOccurrence reloaded =
                occurrenceRepository.findById(saved.getId()).orElseThrow();
        assertEquals(new BigDecimal("120000.10"), reloaded.getExpectedAmount().getValue());
        assertEquals(RecurringObligationOccurrenceStatus.PENDING, reloaded.getStatus());
        assertNull(reloaded.getPaidDate());
        assertNull(reloaded.getFinancialTransactionId());
        assertEquals("Agosto", reloaded.getObservation());

        UUID transactionId = UUID.randomUUID();
        LocalDateTime paidAt = LocalDateTime.of(2026, 8, 16, 9, 30);
        reloaded.markPaid(transactionId, paidAt);
        occurrenceRepository.save(reloaded);

        entityManager.flush();
        entityManager.clear();

        RecurringFinancialObligationOccurrence paid =
                occurrenceRepository.findById(saved.getId()).orElseThrow();
        assertEquals(RecurringObligationOccurrenceStatus.PAID, paid.getStatus());
        assertEquals(transactionId, paid.getFinancialTransactionId());
        assertEquals(paidAt, paid.getPaidDate());

        RuntimeException duplicate = assertThrows(RuntimeException.class, () -> {
            occurrenceRepository.save(
                    RecurringFinancialObligationOccurrence.createPending(
                            obligation.getId(),
                            LocalDate.of(2026, 8, 15),
                            FinancialAmount.of(new BigDecimal("120000.10")),
                            null
                    )
            );
            entityManager.flush();
        });
        assertTrue(
                duplicate.getMessage() != null
                        && duplicate.getMessage().toLowerCase().contains("uq_recurring_financial_obligation_occurrences_obligation_due")
                        || (duplicate.getCause() != null
                        && duplicate.getCause().getMessage() != null
                        && duplicate.getCause().getMessage().toLowerCase()
                        .contains("uq_recurring_financial_obligation_occurrences_obligation_due")),
                "Expected unique constraint violation for obligation/due date"
        );
    }
}
