package com.magyen.platform.finance.domain;

import com.magyen.platform.finance.domain.exception.FinanceDomainException;
import com.magyen.platform.finance.domain.exception.RecurringObligationOccurrenceAlreadyPaidException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RecurringFinancialObligationOccurrenceTest {

    @Test
    void createsValidPendingOccurrence() {
        UUID obligationId = UUID.randomUUID();
        RecurringFinancialObligationOccurrence occurrence =
                RecurringFinancialObligationOccurrence.createPending(
                        obligationId,
                        LocalDate.of(2026, 8, 15),
                        FinancialAmount.of(new BigDecimal("120000.00")),
                        null
                );

        assertEquals(RecurringObligationOccurrenceStatus.PENDING, occurrence.getStatus());
        assertEquals(new BigDecimal("120000.00"), occurrence.getExpectedAmount().getValue());
        assertNull(occurrence.getPaidDate());
        assertNull(occurrence.getFinancialTransactionId());
    }

    @Test
    void pendingCanBePaid() {
        RecurringFinancialObligationOccurrence occurrence = pendingOccurrence();
        UUID transactionId = UUID.randomUUID();
        LocalDateTime paidAt = LocalDateTime.of(2026, 8, 15, 10, 0);

        occurrence.markPaid(transactionId, paidAt);

        assertEquals(RecurringObligationOccurrenceStatus.PAID, occurrence.getStatus());
        assertEquals(transactionId, occurrence.getFinancialTransactionId());
        assertEquals(paidAt, occurrence.getPaidDate());
    }

    @Test
    void pendingCanBeCancelled() {
        RecurringFinancialObligationOccurrence occurrence = pendingOccurrence();
        occurrence.cancel();
        assertEquals(RecurringObligationOccurrenceStatus.CANCELLED, occurrence.getStatus());
    }

    @Test
    void paidCannotBeCancelled() {
        RecurringFinancialObligationOccurrence occurrence = pendingOccurrence();
        occurrence.markPaid(UUID.randomUUID(), LocalDateTime.now());
        assertThrows(FinanceDomainException.class, occurrence::cancel);
    }

    @Test
    void paidCannotBePaidTwice() {
        RecurringFinancialObligationOccurrence occurrence = pendingOccurrence();
        occurrence.markPaid(UUID.randomUUID(), LocalDateTime.now());
        assertThrows(
                RecurringObligationOccurrenceAlreadyPaidException.class,
                () -> occurrence.markPaid(UUID.randomUUID(), LocalDateTime.now())
        );
    }

    @Test
    void cancelledCannotBePaid() {
        RecurringFinancialObligationOccurrence occurrence = pendingOccurrence();
        occurrence.cancel();
        assertThrows(
                FinanceDomainException.class,
                () -> occurrence.markPaid(UUID.randomUUID(), LocalDateTime.now())
        );
    }

    @Test
    void rejectsInvalidAmount() {
        assertThrows(FinanceDomainException.class, () -> FinancialAmount.of(BigDecimal.ZERO));
    }

    @Test
    void rejectsInvalidDueDateAgainstObligation() {
        RecurringFinancialObligation obligation = RecurringFinancialObligation.create(
                "Internet",
                RecurringObligationType.SERVICE,
                FinancialAmount.of(new BigDecimal("120000.00")),
                RecurringObligationFrequency.MONTHLY,
                15,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 12, 31),
                null,
                null
        );

        assertThrows(
                FinanceDomainException.class,
                () -> obligation.requireCompatibleOccurrenceDueDate(LocalDate.of(2026, 8, 10))
        );
        assertThrows(
                FinanceDomainException.class,
                () -> obligation.requireCompatibleOccurrenceDueDate(LocalDate.of(2026, 7, 15))
        );
        obligation.requireCompatibleOccurrenceDueDate(LocalDate.of(2026, 8, 15));
    }

    @Test
    void snapshotAmountRemainsUnchangedWhenObligationAmountChanges() {
        RecurringFinancialObligation obligation = RecurringFinancialObligation.create(
                "Internet",
                RecurringObligationType.SERVICE,
                FinancialAmount.of(new BigDecimal("120000.00")),
                RecurringObligationFrequency.MONTHLY,
                15,
                LocalDate.of(2026, 8, 1),
                null,
                null,
                null
        );

        RecurringFinancialObligationOccurrence occurrence =
                RecurringFinancialObligationOccurrence.createPending(
                        obligation.getId(),
                        LocalDate.of(2026, 8, 15),
                        obligation.getExpectedAmount(),
                        null
                );

        obligation.update(
                "Internet",
                RecurringObligationType.SERVICE,
                FinancialAmount.of(new BigDecimal("150000.00")),
                RecurringObligationFrequency.MONTHLY,
                15,
                LocalDate.of(2026, 8, 1),
                null,
                null,
                null
        );

        assertEquals(new BigDecimal("120000.00"), occurrence.getExpectedAmount().getValue());
        assertEquals(new BigDecimal("150000.00"), obligation.getExpectedAmount().getValue());
    }

    private static RecurringFinancialObligationOccurrence pendingOccurrence() {
        return RecurringFinancialObligationOccurrence.createPending(
                UUID.randomUUID(),
                LocalDate.of(2026, 8, 15),
                FinancialAmount.of(new BigDecimal("120000.00")),
                null
        );
    }
}
