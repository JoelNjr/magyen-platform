package com.magyen.platform.finance.domain;

import com.magyen.platform.finance.domain.exception.FinanceDomainException;
import com.magyen.platform.finance.domain.exception.RecurringObligationOccurrenceAlreadyPaidException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root de una ocurrencia concreta de una obligación recurrente.
 * <p>
 * Conserva un snapshot del monto esperado al momento de creación.
 * Su existencia NO crea {@link FinancialTransaction}; el pago explícito sí.
 */
public class RecurringFinancialObligationOccurrence {

    private static final int MAX_TEXT_LENGTH = 2000;

    private final UUID id;
    private final UUID recurringObligationId;
    private final LocalDate dueDate;
    private final FinancialAmount expectedAmount;
    private RecurringObligationOccurrenceStatus status;
    private LocalDateTime paidDate;
    private UUID financialTransactionId;
    private String observation;

    private RecurringFinancialObligationOccurrence(
            UUID id,
            UUID recurringObligationId,
            LocalDate dueDate,
            FinancialAmount expectedAmount,
            RecurringObligationOccurrenceStatus status,
            LocalDateTime paidDate,
            UUID financialTransactionId,
            String observation
    ) {
        this.id = Objects.requireNonNull(id, "Occurrence id must not be null");
        this.recurringObligationId = Objects.requireNonNull(
                recurringObligationId,
                "Recurring obligation id must not be null"
        );
        this.dueDate = Objects.requireNonNull(dueDate, "Due date must not be null");
        this.expectedAmount = Objects.requireNonNull(expectedAmount, "Expected amount must not be null");
        this.status = Objects.requireNonNull(status, "Status must not be null");
        this.paidDate = paidDate;
        this.financialTransactionId = financialTransactionId;
        this.observation = normalizeOptionalText(observation);

        requireConsistentPaymentState();
    }

    /**
     * Crea una ocurrencia PENDING con snapshot del monto esperado.
     * <p>
     * No genera movimientos del ledger.
     */
    public static RecurringFinancialObligationOccurrence createPending(
            UUID recurringObligationId,
            LocalDate dueDate,
            FinancialAmount expectedAmount,
            String observation
    ) {
        return new RecurringFinancialObligationOccurrence(
                UUID.randomUUID(),
                recurringObligationId,
                dueDate,
                expectedAmount,
                RecurringObligationOccurrenceStatus.PENDING,
                null,
                null,
                observation
        );
    }

    /**
     * Reconstruye desde persistencia. No aplica lógica de creación de negocio.
     */
    public static RecurringFinancialObligationOccurrence reconstitute(
            UUID id,
            UUID recurringObligationId,
            LocalDate dueDate,
            FinancialAmount expectedAmount,
            RecurringObligationOccurrenceStatus status,
            LocalDateTime paidDate,
            UUID financialTransactionId,
            String observation
    ) {
        return new RecurringFinancialObligationOccurrence(
                id,
                recurringObligationId,
                dueDate,
                expectedAmount,
                status,
                paidDate,
                financialTransactionId,
                observation
        );
    }

    /**
     * Marca la ocurrencia como pagada y asocia el movimiento del ledger generado.
     */
    public void markPaid(UUID financialTransactionId, LocalDateTime paidDate) {
        if (status == RecurringObligationOccurrenceStatus.PAID) {
            throw new RecurringObligationOccurrenceAlreadyPaidException();
        }
        if (status != RecurringObligationOccurrenceStatus.PENDING) {
            throw new FinanceDomainException(
                    "Only PENDING occurrences can be paid. Current status: " + status
            );
        }
        Objects.requireNonNull(financialTransactionId, "Financial transaction id must not be null");
        Objects.requireNonNull(paidDate, "Paid date must not be null");

        this.status = RecurringObligationOccurrenceStatus.PAID;
        this.financialTransactionId = financialTransactionId;
        this.paidDate = paidDate;
    }

    /**
     * Cancela una ocurrencia pendiente. No genera movimientos del ledger.
     */
    public void cancel() {
        if (status == RecurringObligationOccurrenceStatus.PAID) {
            throw new FinanceDomainException("A PAID occurrence cannot be cancelled");
        }
        if (status == RecurringObligationOccurrenceStatus.CANCELLED) {
            throw new FinanceDomainException("Occurrence is already cancelled");
        }
        this.status = RecurringObligationOccurrenceStatus.CANCELLED;
    }

    public UUID getId() {
        return id;
    }

    public UUID getRecurringObligationId() {
        return recurringObligationId;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public FinancialAmount getExpectedAmount() {
        return expectedAmount;
    }

    public RecurringObligationOccurrenceStatus getStatus() {
        return status;
    }

    public LocalDateTime getPaidDate() {
        return paidDate;
    }

    public UUID getFinancialTransactionId() {
        return financialTransactionId;
    }

    public String getObservation() {
        return observation;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        RecurringFinancialObligationOccurrence that = (RecurringFinancialObligationOccurrence) other;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private void requireConsistentPaymentState() {
        if (status == RecurringObligationOccurrenceStatus.PAID) {
            if (paidDate == null || financialTransactionId == null) {
                throw new FinanceDomainException(
                        "PAID occurrence must have paid date and financial transaction id"
                );
            }
        }
        if (status == RecurringObligationOccurrenceStatus.PENDING
                || status == RecurringObligationOccurrenceStatus.CANCELLED) {
            if (paidDate != null || financialTransactionId != null) {
                throw new FinanceDomainException(
                        "Non-PAID occurrence must not have paid date or financial transaction id"
                );
            }
        }
    }

    private static String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > MAX_TEXT_LENGTH) {
            throw new FinanceDomainException("Observation must not exceed " + MAX_TEXT_LENGTH + " characters");
        }
        return normalized;
    }
}
