package com.magyen.platform.finance.infrastructure.persistence.entity;

import com.magyen.platform.finance.domain.SellerCommissionSettlementStatus;
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
 * Modelo relacional de {@link com.magyen.platform.finance.domain.SellerCommissionSettlement}.
 * <p>
 * {@code employee_id} y {@code financial_transaction_id} son referencias UUID blandas.
 */
@Entity
@Table(name = "seller_commission_settlements")
public class SellerCommissionSettlementEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "employee_id", nullable = false, updatable = false)
    private UUID employeeId;

    @Column(name = "period_start", nullable = false, updatable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false, updatable = false)
    private LocalDate periodEnd;

    @Column(name = "sales_snapshot", nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal salesSnapshot;

    @Column(name = "order_count_snapshot", nullable = false, updatable = false)
    private int orderCountSnapshot;

    @Column(name = "commission_snapshot", nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal commissionSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30, updatable = false)
    private SellerCommissionSettlementStatus status;

    @Column(name = "actual_payment_date", nullable = false, updatable = false)
    private LocalDate actualPaymentDate;

    @Column(name = "paid_at", nullable = false, updatable = false)
    private LocalDateTime paidAt;

    @Column(name = "financial_transaction_id", nullable = false, updatable = false)
    private UUID financialTransactionId;

    @Column(name = "observation", length = 2000)
    private String observation;

    public SellerCommissionSettlementEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(UUID employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDate periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }

    public BigDecimal getSalesSnapshot() {
        return salesSnapshot;
    }

    public void setSalesSnapshot(BigDecimal salesSnapshot) {
        this.salesSnapshot = salesSnapshot;
    }

    public int getOrderCountSnapshot() {
        return orderCountSnapshot;
    }

    public void setOrderCountSnapshot(int orderCountSnapshot) {
        this.orderCountSnapshot = orderCountSnapshot;
    }

    public BigDecimal getCommissionSnapshot() {
        return commissionSnapshot;
    }

    public void setCommissionSnapshot(BigDecimal commissionSnapshot) {
        this.commissionSnapshot = commissionSnapshot;
    }

    public SellerCommissionSettlementStatus getStatus() {
        return status;
    }

    public void setStatus(SellerCommissionSettlementStatus status) {
        this.status = status;
    }

    public LocalDate getActualPaymentDate() {
        return actualPaymentDate;
    }

    public void setActualPaymentDate(LocalDate actualPaymentDate) {
        this.actualPaymentDate = actualPaymentDate;
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

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }
}
