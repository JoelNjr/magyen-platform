package com.magyen.platform.finance.infrastructure.persistence.entity;

import com.magyen.platform.finance.domain.PayrollDeductionStatus;
import com.magyen.platform.finance.domain.PayrollDeductionType;
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
 * Modelo relacional del agregado {@link com.magyen.platform.finance.domain.PayrollDeduction}.
 * <p>
 * {@code employee_id} es UUID suave a {@code payroll_employees.id}; sin FK JPA.
 */
@Entity
@Table(name = "payroll_deductions")
public class PayrollDeductionEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "employee_id", nullable = false, updatable = false)
    private UUID employeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30, updatable = false)
    private PayrollDeductionType type;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal amount;

    @Column(name = "deduction_date", nullable = false, updatable = false)
    private LocalDate deductionDate;

    @Column(name = "description", length = 2000, updatable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PayrollDeductionStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public PayrollDeductionEntity() {
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

    public PayrollDeductionType getType() {
        return type;
    }

    public void setType(PayrollDeductionType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getDeductionDate() {
        return deductionDate;
    }

    public void setDeductionDate(LocalDate deductionDate) {
        this.deductionDate = deductionDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PayrollDeductionStatus getStatus() {
        return status;
    }

    public void setStatus(PayrollDeductionStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
