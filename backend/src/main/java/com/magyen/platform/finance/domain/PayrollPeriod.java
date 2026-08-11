package com.magyen.platform.finance.domain;

import com.magyen.platform.finance.domain.exception.FinanceDomainException;
import com.magyen.platform.finance.domain.exception.PayrollPeriodAlreadyPaidException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Período/pago de nómina fija.
 * <p>
 * Congela el monto al generarse. Su creación NO crea {@link FinancialTransaction};
 * solo {@link #markPaid(UUID, LocalDateTime, LocalDate)} genera el gasto de caja.
 * El {@code id} es la identidad usada como {@code sourceId} del ledger ({@code PAYROLL}).
 */
public class PayrollPeriod {

    private final UUID id;
    private final UUID employeeId;
    private final LocalDate periodStart;
    private final LocalDate periodEnd;
    private final LocalDate expectedPaymentDate;
    private final FinancialAmount amountSnapshot;
    private PayrollPeriodStatus status;
    private LocalDate actualPaymentDate;
    private LocalDateTime paidAt;
    private UUID financialTransactionId;

    private PayrollPeriod(
            UUID id,
            UUID employeeId,
            LocalDate periodStart,
            LocalDate periodEnd,
            LocalDate expectedPaymentDate,
            FinancialAmount amountSnapshot,
            PayrollPeriodStatus status,
            LocalDate actualPaymentDate,
            LocalDateTime paidAt,
            UUID financialTransactionId
    ) {
        this.id = Objects.requireNonNull(id, "Payroll period id must not be null");
        this.employeeId = Objects.requireNonNull(employeeId, "Employee id must not be null");
        this.periodStart = Objects.requireNonNull(periodStart, "Period start must not be null");
        this.periodEnd = Objects.requireNonNull(periodEnd, "Period end must not be null");
        this.expectedPaymentDate = Objects.requireNonNull(
                expectedPaymentDate,
                "Expected payment date must not be null"
        );
        this.amountSnapshot = Objects.requireNonNull(amountSnapshot, "Amount snapshot must not be null");
        this.status = Objects.requireNonNull(status, "Status must not be null");
        this.actualPaymentDate = actualPaymentDate;
        this.paidAt = paidAt;
        this.financialTransactionId = financialTransactionId;

        if (periodEnd.isBefore(periodStart)) {
            throw new FinanceDomainException("Period end must not be before period start");
        }
        requireConsistentPaymentState();
    }

    public static PayrollPeriod createPending(
            UUID employeeId,
            LocalDate periodStart,
            LocalDate periodEnd,
            LocalDate expectedPaymentDate,
            FinancialAmount amountSnapshot
    ) {
        return new PayrollPeriod(
                UUID.randomUUID(),
                employeeId,
                periodStart,
                periodEnd,
                expectedPaymentDate,
                amountSnapshot,
                PayrollPeriodStatus.PENDING,
                null,
                null,
                null
        );
    }

    public static PayrollPeriod reconstitute(
            UUID id,
            UUID employeeId,
            LocalDate periodStart,
            LocalDate periodEnd,
            LocalDate expectedPaymentDate,
            FinancialAmount amountSnapshot,
            PayrollPeriodStatus status,
            LocalDate actualPaymentDate,
            LocalDateTime paidAt,
            UUID financialTransactionId
    ) {
        return new PayrollPeriod(
                id,
                employeeId,
                periodStart,
                periodEnd,
                expectedPaymentDate,
                amountSnapshot,
                status,
                actualPaymentDate,
                paidAt,
                financialTransactionId
        );
    }

    public void markPaid(UUID financialTransactionId, LocalDateTime paidAt, LocalDate actualPaymentDate) {
        if (status == PayrollPeriodStatus.PAID) {
            throw new PayrollPeriodAlreadyPaidException();
        }
        if (status != PayrollPeriodStatus.PENDING) {
            throw new FinanceDomainException(
                    "Only PENDING payroll periods can be paid. Current status: " + status
            );
        }
        Objects.requireNonNull(financialTransactionId, "Financial transaction id must not be null");
        Objects.requireNonNull(paidAt, "Paid at must not be null");
        Objects.requireNonNull(actualPaymentDate, "Actual payment date must not be null");

        this.status = PayrollPeriodStatus.PAID;
        this.financialTransactionId = financialTransactionId;
        this.paidAt = paidAt;
        this.actualPaymentDate = actualPaymentDate;
    }

    public void cancel() {
        if (status == PayrollPeriodStatus.PAID) {
            throw new FinanceDomainException("A PAID payroll period cannot be cancelled");
        }
        if (status == PayrollPeriodStatus.CANCELLED) {
            throw new FinanceDomainException("Payroll period is already cancelled");
        }
        this.status = PayrollPeriodStatus.CANCELLED;
    }

    public UUID getId() {
        return id;
    }

    public UUID getEmployeeId() {
        return employeeId;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public LocalDate getExpectedPaymentDate() {
        return expectedPaymentDate;
    }

    public FinancialAmount getAmountSnapshot() {
        return amountSnapshot;
    }

    public PayrollPeriodStatus getStatus() {
        return status;
    }

    public LocalDate getActualPaymentDate() {
        return actualPaymentDate;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
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
        PayrollPeriod that = (PayrollPeriod) other;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private void requireConsistentPaymentState() {
        if (status == PayrollPeriodStatus.PAID) {
            if (paidAt == null || financialTransactionId == null || actualPaymentDate == null) {
                throw new FinanceDomainException(
                        "PAID payroll period must have paidAt, actualPaymentDate and financialTransactionId"
                );
            }
        }
        if (status == PayrollPeriodStatus.PENDING || status == PayrollPeriodStatus.CANCELLED) {
            if (paidAt != null || financialTransactionId != null || actualPaymentDate != null) {
                throw new FinanceDomainException(
                        "Non-PAID payroll period must not have payment fields"
                );
            }
        }
    }
}
