package com.magyen.platform.finance.infrastructure.persistence.entity;

import com.magyen.platform.finance.domain.RecurringObligationOccurrenceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modelo relacional de {@link com.magyen.platform.finance.domain.RecurringFinancialObligationOccurrence}.
 * <p>
 * {@code recurring_obligation_id} es referencia UUID blanda; sin FK JPA al agregado de obligación.
 */
@Entity
@Table(name = "recurring_financial_obligation_occurrences")
public class RecurringFinancialObligationOccurrenceEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "recurring_obligation_id", nullable = false, updatable = false)
    private UUID recurringObligationId;

    @Column(name = "due_date", nullable = false, updatable = false)
    private LocalDate dueDate;

    @Column(name = "expected_amount", nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal expectedAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RecurringObligationOccurrenceStatus status;

    @Column(name = "paid_date")
    private LocalDateTime paidDate;

    @Column(name = "financial_transaction_id")
    private UUID financialTransactionId;

    @Column(name = "observation", length = 2000)
    private String observation;

    public RecurringFinancialObligationOccurrenceEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getRecurringObligationId() {
        return recurringObligationId;
    }

    public void setRecurringObligationId(UUID recurringObligationId) {
        this.recurringObligationId = recurringObligationId;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public BigDecimal getExpectedAmount() {
        return expectedAmount;
    }

    public void setExpectedAmount(BigDecimal expectedAmount) {
        this.expectedAmount = expectedAmount;
    }

    public RecurringObligationOccurrenceStatus getStatus() {
        return status;
    }

    public void setStatus(RecurringObligationOccurrenceStatus status) {
        this.status = status;
    }

    public LocalDateTime getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(LocalDateTime paidDate) {
        this.paidDate = paidDate;
    }

    public UUID getFinancialTransactionId() {
        return financialTransactionId;
    }

    public void setFinancialTransactionId(UUID financialTransactionId) {
        this.financialTransactionId = financialTransactionId;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }
}
