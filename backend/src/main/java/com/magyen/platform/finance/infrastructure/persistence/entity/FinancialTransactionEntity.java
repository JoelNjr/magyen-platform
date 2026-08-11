package com.magyen.platform.finance.infrastructure.persistence.entity;

import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Modelo relacional del agregado {@link com.magyen.platform.finance.domain.FinancialTransaction}.
 * <p>
 * Conserva {@code source_id} como referencia UUID blanda; sin FKs a otros módulos.
 */
@Entity
@Table(name = "financial_transactions")
public class FinancialTransactionEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    private FinancialTransactionType transactionType;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "category", nullable = false, length = 2000)
    private String category;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "observation", length = 2000)
    private String observation;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", length = 30)
    private FinancialTransactionSourceType sourceType;

    @Column(name = "source_id")
    private UUID sourceId;

    public FinancialTransactionEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public FinancialTransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(FinancialTransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public FinancialTransactionSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(FinancialTransactionSourceType sourceType) {
        this.sourceType = sourceType;
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public void setSourceId(UUID sourceId) {
        this.sourceId = sourceId;
    }
}
