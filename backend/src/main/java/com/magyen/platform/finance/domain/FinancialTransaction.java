package com.magyen.platform.finance.domain;

import com.magyen.platform.finance.domain.exception.FinanceDomainException;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root del ledger financiero.
 * <p>
 * Representa un movimiento de ingreso o gasto. Conserva metadatos de origen
 * como referencias UUID blandas, sin depender de otros módulos.
 */
public class FinancialTransaction {

    private static final int MAX_TEXT_LENGTH = 2000;

    private final UUID id;
    private final FinancialTransactionType type;
    private final FinancialAmount amount;
    private final LocalDate transactionDate;
    private final String category;
    private final String description;
    private final String observation;
    private final FinancialTransactionSourceType sourceType;
    private final UUID sourceId;

    private FinancialTransaction(
            UUID id,
            FinancialTransactionType type,
            FinancialAmount amount,
            LocalDate transactionDate,
            String category,
            String description,
            String observation,
            FinancialTransactionSourceType sourceType,
            UUID sourceId
    ) {
        this.id = Objects.requireNonNull(id, "Financial transaction id must not be null");
        this.type = Objects.requireNonNull(type, "Transaction type must not be null");
        this.amount = Objects.requireNonNull(amount, "Financial amount must not be null");
        this.transactionDate = Objects.requireNonNull(transactionDate, "Transaction date must not be null");
        this.category = requireCategory(category);
        this.description = normalizeOptionalText(description, "Description");
        this.observation = normalizeOptionalText(observation, "Observation");
        this.sourceType = Objects.requireNonNull(sourceType, "Source type must not be null");
        this.sourceId = sourceId;
    }

    /**
     * Registra un movimiento financiero válido en el ledger.
     */
    public static FinancialTransaction create(
            FinancialTransactionType type,
            FinancialAmount amount,
            LocalDate transactionDate,
            String category,
            String description,
            String observation,
            FinancialTransactionSourceType sourceType,
            UUID sourceId
    ) {
        FinancialTransactionSourceType effectiveSourceType = sourceType == null
                ? FinancialTransactionSourceType.MANUAL
                : sourceType;

        return new FinancialTransaction(
                UUID.randomUUID(),
                type,
                amount,
                transactionDate,
                category,
                description,
                observation,
                effectiveSourceType,
                sourceId
        );
    }

    /**
     * Reconstruye un movimiento desde persistencia. No aplica lógica de creación de negocio.
     */
    public static FinancialTransaction reconstitute(
            UUID id,
            FinancialTransactionType type,
            FinancialAmount amount,
            LocalDate transactionDate,
            String category,
            String description,
            String observation,
            FinancialTransactionSourceType sourceType,
            UUID sourceId
    ) {
        return new FinancialTransaction(
                id,
                type,
                amount,
                transactionDate,
                category,
                description,
                observation,
                sourceType == null ? FinancialTransactionSourceType.MANUAL : sourceType,
                sourceId
        );
    }

    public UUID getId() {
        return id;
    }

    public FinancialTransactionType getType() {
        return type;
    }

    public FinancialAmount getAmount() {
        return amount;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getObservation() {
        return observation;
    }

    public FinancialTransactionSourceType getSourceType() {
        return sourceType;
    }

    public UUID getSourceId() {
        return sourceId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        FinancialTransaction that = (FinancialTransaction) other;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private static String requireCategory(String category) {
        if (category == null || category.isBlank()) {
            throw new FinanceDomainException("Category must not be blank");
        }
        String normalized = category.trim();
        if (normalized.length() > MAX_TEXT_LENGTH) {
            throw new FinanceDomainException("Category must not exceed " + MAX_TEXT_LENGTH + " characters");
        }
        return normalized;
    }

    private static String normalizeOptionalText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > MAX_TEXT_LENGTH) {
            throw new FinanceDomainException(fieldName + " must not exceed " + MAX_TEXT_LENGTH + " characters");
        }
        return normalized;
    }
}
