package com.magyen.platform.production.infrastructure.persistence.entity;

import com.magyen.platform.production.domain.ProductionDirectCostCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Modelo relacional de {@link com.magyen.platform.production.domain.ProductionAdditionalCost}.
 */
@Entity
@Table(name = "production_additional_costs")
public class ProductionAdditionalCostEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "production_order_id", nullable = false)
    private ProductionOrderEntity productionOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30, updatable = false)
    private ProductionDirectCostCategory category;

    @Column(name = "description", nullable = false, length = 2000, updatable = false)
    private String description;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal amount;

    @Column(name = "incurred_date", nullable = false, updatable = false)
    private LocalDate incurredDate;

    @Column(name = "financial_transaction_id")
    private UUID financialTransactionId;

    public ProductionAdditionalCostEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ProductionOrderEntity getProductionOrder() {
        return productionOrder;
    }

    public void setProductionOrder(ProductionOrderEntity productionOrder) {
        this.productionOrder = productionOrder;
    }

    public ProductionDirectCostCategory getCategory() {
        return category;
    }

    public void setCategory(ProductionDirectCostCategory category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getIncurredDate() {
        return incurredDate;
    }

    public void setIncurredDate(LocalDate incurredDate) {
        this.incurredDate = incurredDate;
    }

    public UUID getFinancialTransactionId() {
        return financialTransactionId;
    }

    public void setFinancialTransactionId(UUID financialTransactionId) {
        this.financialTransactionId = financialTransactionId;
    }
}
