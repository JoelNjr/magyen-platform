package com.magyen.platform.finance.domain;

import com.magyen.platform.finance.domain.exception.FinanceDomainException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Liquidación pagada de la comisión de un vendedor en un mes de confirmación.
 * <p>
 * El monto queda congelado al pagar. No se recalcula si el pedido cambia después.
 * No crea el movimiento de caja por sí sola: el caso de uso persiste el gasto
 * en la misma transacción y guarda su identidad aquí.
 */
public class SellerCommissionSettlement {

    public static final LocalDate PAYABLE_FROM = LocalDate.of(2026, 9, 1);
    private static final int MAX_OBSERVATION_LENGTH = 2000;

    private final UUID id;
    private final UUID employeeId;
    private final LocalDate periodStart;
    private final LocalDate periodEnd;
    private final FinancialAmount salesSnapshot;
    private final int orderCountSnapshot;
    private final FinancialAmount commissionSnapshot;
    private final SellerCommissionSettlementStatus status;
    private final LocalDate actualPaymentDate;
    private final LocalDateTime paidAt;
    private final UUID financialTransactionId;
    private final String observation;

    private SellerCommissionSettlement(
            UUID id,
            UUID employeeId,
            LocalDate periodStart,
            LocalDate periodEnd,
            FinancialAmount salesSnapshot,
            int orderCountSnapshot,
            FinancialAmount commissionSnapshot,
            SellerCommissionSettlementStatus status,
            LocalDate actualPaymentDate,
            LocalDateTime paidAt,
            UUID financialTransactionId,
            String observation
    ) {
        this.id = Objects.requireNonNull(id, "Settlement id must not be null");
        this.employeeId = Objects.requireNonNull(employeeId, "Employee id must not be null");
        this.periodStart = Objects.requireNonNull(periodStart, "Period start must not be null");
        this.periodEnd = Objects.requireNonNull(periodEnd, "Period end must not be null");
        this.salesSnapshot = Objects.requireNonNull(salesSnapshot, "Sales snapshot must not be null");
        this.orderCountSnapshot = orderCountSnapshot;
        this.commissionSnapshot = Objects.requireNonNull(commissionSnapshot, "Commission snapshot must not be null");
        this.status = Objects.requireNonNull(status, "Status must not be null");
        this.actualPaymentDate = Objects.requireNonNull(actualPaymentDate, "Actual payment date must not be null");
        this.paidAt = Objects.requireNonNull(paidAt, "Paid at must not be null");
        this.financialTransactionId = Objects.requireNonNull(
                financialTransactionId,
                "Financial transaction id must not be null"
        );
        this.observation = normalizeObservation(observation);
        validatePayableMonth();
        validateSnapshot();
        if (status != SellerCommissionSettlementStatus.PAID) {
            throw new FinanceDomainException("A seller commission settlement is created only as PAID");
        }
    }

    public static SellerCommissionSettlement createPaid(
            UUID id,
            UUID employeeId,
            LocalDate periodStart,
            LocalDate periodEnd,
            FinancialAmount salesSnapshot,
            int orderCountSnapshot,
            FinancialAmount commissionSnapshot,
            LocalDate actualPaymentDate,
            LocalDateTime paidAt,
            UUID financialTransactionId,
            String observation
    ) {
        return new SellerCommissionSettlement(
                id,
                employeeId,
                periodStart,
                periodEnd,
                salesSnapshot,
                orderCountSnapshot,
                commissionSnapshot,
                SellerCommissionSettlementStatus.PAID,
                actualPaymentDate,
                paidAt,
                financialTransactionId,
                observation
        );
    }

    public static SellerCommissionSettlement reconstitute(
            UUID id,
            UUID employeeId,
            LocalDate periodStart,
            LocalDate periodEnd,
            FinancialAmount salesSnapshot,
            int orderCountSnapshot,
            FinancialAmount commissionSnapshot,
            SellerCommissionSettlementStatus status,
            LocalDate actualPaymentDate,
            LocalDateTime paidAt,
            UUID financialTransactionId,
            String observation
    ) {
        return new SellerCommissionSettlement(
                id,
                employeeId,
                periodStart,
                periodEnd,
                salesSnapshot,
                orderCountSnapshot,
                commissionSnapshot,
                status,
                actualPaymentDate,
                paidAt,
                financialTransactionId,
                observation
        );
    }

    public static LocalDate periodEndOf(LocalDate periodStart) {
        Objects.requireNonNull(periodStart, "Period start must not be null");
        if (!periodStart.equals(periodStart.withDayOfMonth(1))) {
            throw new FinanceDomainException("Commission periodStart must be the first day of a calendar month");
        }
        return periodStart.withDayOfMonth(periodStart.lengthOfMonth());
    }

    public static void requirePayableMonth(LocalDate periodStart) {
        LocalDate periodEnd = periodEndOf(periodStart);
        if (periodStart.isBefore(PAYABLE_FROM)) {
            throw new FinanceDomainException("Commission periods before September 2026 are not payable");
        }
        if (!periodEnd.equals(periodStart.withDayOfMonth(periodStart.lengthOfMonth()))) {
            throw new FinanceDomainException("Commission period must be exactly one calendar month");
        }
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

    public FinancialAmount getSalesSnapshot() {
        return salesSnapshot;
    }

    public int getOrderCountSnapshot() {
        return orderCountSnapshot;
    }

    public FinancialAmount getCommissionSnapshot() {
        return commissionSnapshot;
    }

    public SellerCommissionSettlementStatus getStatus() {
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

    public String getObservation() {
        return observation;
    }

    private void validatePayableMonth() {
        if (!periodStart.equals(periodStart.withDayOfMonth(1))) {
            throw new FinanceDomainException("Commission periodStart must be the first day of a calendar month");
        }
        LocalDate expectedEnd = periodStart.withDayOfMonth(periodStart.lengthOfMonth());
        if (!periodEnd.equals(expectedEnd)) {
            throw new FinanceDomainException("Commission periodEnd must be the last day of the same month");
        }
        if (periodStart.isBefore(PAYABLE_FROM)) {
            throw new FinanceDomainException("Commission periods before September 2026 are not payable");
        }
    }

    private void validateSnapshot() {
        if (orderCountSnapshot < 1) {
            throw new FinanceDomainException("A paid commission settlement requires at least one order");
        }
    }

    private static String normalizeObservation(String observation) {
        if (observation == null || observation.isBlank()) {
            return null;
        }
        String normalized = observation.trim();
        if (normalized.length() > MAX_OBSERVATION_LENGTH) {
            throw new FinanceDomainException("Observation must not exceed 2000 characters");
        }
        return normalized;
    }
}
