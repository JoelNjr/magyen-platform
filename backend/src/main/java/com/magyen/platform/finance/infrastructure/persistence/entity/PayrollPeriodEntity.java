package com.magyen.platform.finance.infrastructure.persistence.entity;

import com.magyen.platform.finance.domain.PayrollPeriodStatus;
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
 * Modelo relacional de {@link com.magyen.platform.finance.domain.PayrollPeriod}.
 * <p>
 * {@code employee_id} es referencia UUID blanda; sin FK JPA al agregado de empleado.
 */
@Entity
@Table(name = "payroll_periods")
public class PayrollPeriodEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "employee_id", nullable = false, updatable = false)
    private UUID employeeId;

    @Column(name = "period_start", nullable = false, updatable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false, updatable = false)
    private LocalDate periodEnd;

    @Column(name = "expected_payment_date", nullable = false, updatable = false)
    private LocalDate expectedPaymentDate;

    @Column(name = "amount_snapshot", nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal amountSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PayrollPeriodStatus status;

    @Column(name = "actual_payment_date")
    private LocalDate actualPaymentDate;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "financial_transaction_id")
    private UUID financialTransactionId;

    public PayrollPeriodEntity() {
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

    public LocalDate getExpectedPaymentDate() {
        return expectedPaymentDate;
    }

    public void setExpectedPaymentDate(LocalDate expectedPaymentDate) {
        this.expectedPaymentDate = expectedPaymentDate;
    }

    public BigDecimal getAmountSnapshot() {
        return amountSnapshot;
    }

    public void setAmountSnapshot(BigDecimal amountSnapshot) {
        this.amountSnapshot = amountSnapshot;
    }

    public PayrollPeriodStatus getStatus() {
        return status;
    }

    public void setStatus(PayrollPeriodStatus status) {
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
}
