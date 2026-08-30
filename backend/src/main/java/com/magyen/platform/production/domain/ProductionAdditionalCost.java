package com.magyen.platform.production.domain;

import com.magyen.platform.production.domain.exception.ProductionDomainException;
import com.magyen.platform.shared.domain.Money;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Costo directo adicional de una Orden de Producción (por ejemplo envíos o empaques).
 * <p>
 * No sustituye material ni mano de obra. La categoría {@link ProductionDirectCostCategory#OTHER}
 * exige descripción libre. El movimiento financiero se crea en Application, no aquí.
 */
public class ProductionAdditionalCost {

    private static final int MAX_DESCRIPTION_LENGTH = 2000;

    private final UUID id;
    private final UUID productionOrderId;
    private final ProductionDirectCostCategory category;
    private final String description;
    private final Money amount;
    private final LocalDate incurredDate;
    private UUID financialTransactionId;

    private ProductionAdditionalCost(
            UUID id,
            UUID productionOrderId,
            ProductionDirectCostCategory category,
            String description,
            Money amount,
            LocalDate incurredDate,
            UUID financialTransactionId
    ) {
        this.id = Objects.requireNonNull(id, "Additional cost id must not be null");
        this.productionOrderId = Objects.requireNonNull(
                productionOrderId,
                "Production order id must not be null"
        );
        this.category = Objects.requireNonNull(category, "Category must not be null");
        this.description = requireDescription(description);
        this.amount = requirePositiveAmount(amount);
        this.incurredDate = Objects.requireNonNull(incurredDate, "Incurred date must not be null");
        this.financialTransactionId = financialTransactionId;
    }

    public static ProductionAdditionalCost create(
            UUID productionOrderId,
            ProductionDirectCostCategory category,
            String description,
            Money amount,
            LocalDate incurredDate
    ) {
        return new ProductionAdditionalCost(
                UUID.randomUUID(),
                productionOrderId,
                category,
                description,
                amount,
                incurredDate,
                null
        );
    }

    public static ProductionAdditionalCost reconstitute(
            UUID id,
            UUID productionOrderId,
            ProductionDirectCostCategory category,
            String description,
            Money amount,
            LocalDate incurredDate,
            UUID financialTransactionId
    ) {
        return new ProductionAdditionalCost(
                id,
                productionOrderId,
                category,
                description,
                amount,
                incurredDate,
                financialTransactionId
        );
    }

    public void assignFinancialTransaction(UUID financialTransactionId) {
        Objects.requireNonNull(financialTransactionId, "Financial transaction id must not be null");
        if (this.financialTransactionId != null) {
            throw new ProductionDomainException(
                    "This additional cost already has a financial transaction"
            );
        }
        this.financialTransactionId = financialTransactionId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductionOrderId() {
        return productionOrderId;
    }

    public ProductionDirectCostCategory getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public Money getAmount() {
        return amount;
    }

    public LocalDate getIncurredDate() {
        return incurredDate;
    }

    public UUID getFinancialTransactionId() {
        return financialTransactionId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        ProductionAdditionalCost that = (ProductionAdditionalCost) other;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private static String requireDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new ProductionDomainException("Additional cost description must not be blank");
        }
        String normalized = description.trim();
        if (normalized.length() > MAX_DESCRIPTION_LENGTH) {
            throw new ProductionDomainException(
                    "Additional cost description must not exceed " + MAX_DESCRIPTION_LENGTH + " characters"
            );
        }
        return normalized;
    }

    private static Money requirePositiveAmount(Money amount) {
        Objects.requireNonNull(amount, "Amount must not be null");
        if (amount.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ProductionDomainException("Additional cost amount must be greater than zero");
        }
        return amount;
    }
}
