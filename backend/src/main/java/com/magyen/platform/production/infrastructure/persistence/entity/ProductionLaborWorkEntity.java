package com.magyen.platform.production.infrastructure.persistence.entity;

import com.magyen.platform.production.domain.ProductionLaborWorkStatus;
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
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modelo relacional de {@link com.magyen.platform.production.domain.ProductionLaborWork}.
 * <p>
 * {@code operator_employee_id} es referencia técnica suave: no hay FK a payroll_employees.
 */
@Entity
@Table(name = "production_labor_work")
public class ProductionLaborWorkEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "production_order_id", nullable = false)
    private ProductionOrderEntity productionOrder;

    @Column(name = "operator_employee_id", nullable = false, updatable = false)
    private UUID operatorEmployeeId;

    @Column(name = "work_date", nullable = false, updatable = false)
    private LocalDate workDate;

    @Column(name = "operation", nullable = false, length = 255, updatable = false)
    private String operation;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 4, updatable = false)
    private BigDecimal quantity;

    @Column(name = "unit_of_measure", nullable = false, length = 50, updatable = false)
    private String unitOfMeasure;

    @Column(name = "unit_rate", nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal unitRate;

    @Column(name = "calculated_amount", nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal calculatedAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ProductionLaborWorkStatus status;

    @Column(name = "observation", length = 2000, updatable = false)
    private String observation;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "financial_transaction_id")
    private UUID financialTransactionId;

    public ProductionLaborWorkEntity() {
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

    public UUID getOperatorEmployeeId() {
        return operatorEmployeeId;
    }

    public void setOperatorEmployeeId(UUID operatorEmployeeId) {
        this.operatorEmployeeId = operatorEmployeeId;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public BigDecimal getUnitRate() {
        return unitRate;
    }

    public void setUnitRate(BigDecimal unitRate) {
        this.unitRate = unitRate;
    }

    public BigDecimal getCalculatedAmount() {
        return calculatedAmount;
    }

    public void setCalculatedAmount(BigDecimal calculatedAmount) {
        this.calculatedAmount = calculatedAmount;
    }

    public ProductionLaborWorkStatus getStatus() {
        return status;
    }

    public void setStatus(ProductionLaborWorkStatus status) {
        this.status = status;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public UUID getFinancialTransactionId() {
        return financialTransactionId;
    }

    public void setFinancialTransactionId(UUID financialTransactionId) {
        this.financialTransactionId = financialTransactionId;
    }
}
