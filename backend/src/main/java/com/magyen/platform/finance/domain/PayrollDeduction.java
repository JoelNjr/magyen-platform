package com.magyen.platform.finance.domain;

import com.magyen.platform.finance.domain.exception.FinanceDomainException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Descuento de nómina registrado contra un {@link PayrollEmployee}.
 * <p>
 * Representa una obligación del empleado hacia Magyen (préstamo, anticipo u otro).
 * Registrar o cancelar un descuento no crea {@link FinancialTransaction}.
 */
public class PayrollDeduction {

    private static final int MAX_DESCRIPTION_LENGTH = 2000;

    private final UUID id;
    private final UUID employeeId;
    private final PayrollDeductionType type;
    private final FinancialAmount amount;
    private final LocalDate deductionDate;
    private final String description;
    private PayrollDeductionStatus status;
    private final LocalDateTime createdAt;

    private PayrollDeduction(
            UUID id,
            UUID employeeId,
            PayrollDeductionType type,
            FinancialAmount amount,
            LocalDate deductionDate,
            String description,
            PayrollDeductionStatus status,
            LocalDateTime createdAt
    ) {
        this.id = Objects.requireNonNull(id, "Payroll deduction id must not be null");
        this.employeeId = Objects.requireNonNull(employeeId, "Employee id must not be null");
        this.type = Objects.requireNonNull(type, "Payroll deduction type must not be null");
        this.amount = Objects.requireNonNull(amount, "Payroll deduction amount must not be null");
        this.deductionDate = Objects.requireNonNull(deductionDate, "Deduction date must not be null");
        this.description = requireDescription(description);
        this.status = Objects.requireNonNull(status, "Payroll deduction status must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Created at must not be null");
    }

    /**
     * Registra un descuento activo. No genera gasto ni ingreso en el ledger.
     */
    public static PayrollDeduction create(
            UUID employeeId,
            PayrollDeductionType type,
            FinancialAmount amount,
            LocalDate deductionDate,
            String description,
            LocalDateTime createdAt
    ) {
        return new PayrollDeduction(
                UUID.randomUUID(),
                employeeId,
                type,
                amount,
                deductionDate,
                description,
                PayrollDeductionStatus.ACTIVE,
                createdAt
        );
    }

    public static PayrollDeduction reconstitute(
            UUID id,
            UUID employeeId,
            PayrollDeductionType type,
            FinancialAmount amount,
            LocalDate deductionDate,
            String description,
            PayrollDeductionStatus status,
            LocalDateTime createdAt
    ) {
        return new PayrollDeduction(
                id,
                employeeId,
                type,
                amount,
                deductionDate,
                description,
                status,
                createdAt
        );
    }

    /**
     * Cancela el descuento preservando el registro. No borra ni crea asientos.
     */
    public void cancel() {
        if (status == PayrollDeductionStatus.CANCELLED) {
            throw new FinanceDomainException("Payroll deduction is already cancelled");
        }
        this.status = PayrollDeductionStatus.CANCELLED;
    }

    public boolean isActive() {
        return status == PayrollDeductionStatus.ACTIVE;
    }

    public UUID getId() {
        return id;
    }

    public UUID getEmployeeId() {
        return employeeId;
    }

    public PayrollDeductionType getType() {
        return type;
    }

    public FinancialAmount getAmount() {
        return amount;
    }

    public LocalDate getDeductionDate() {
        return deductionDate;
    }

    public String getDescription() {
        return description;
    }

    public PayrollDeductionStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        PayrollDeduction that = (PayrollDeduction) other;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private static String requireDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        String normalized = description.trim();
        if (normalized.length() > MAX_DESCRIPTION_LENGTH) {
            throw new FinanceDomainException(
                    "Description must not exceed " + MAX_DESCRIPTION_LENGTH + " characters"
            );
        }
        return normalized;
    }
}
