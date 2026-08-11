package com.magyen.platform.finance.domain;

import com.magyen.platform.finance.domain.exception.FinanceDomainException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Empleado de nómina Magyen.
 * <p>
 * Solo conserva datos mínimos para compensación. No modela HR sensible.
 * {@link PayrollCompensationType#PRODUCTION_BASED} no participa en generación fija.
 */
public class PayrollEmployee {

    private static final int MAX_DISPLAY_NAME_LENGTH = 255;

    private final UUID id;
    private String displayName;
    private boolean active;
    private PayrollCompensationType compensationType;
    private FinancialAmount fixedAmount;
    private PayrollFrequency frequency;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;

    private PayrollEmployee(
            UUID id,
            String displayName,
            boolean active,
            PayrollCompensationType compensationType,
            FinancialAmount fixedAmount,
            PayrollFrequency frequency,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {
        this.id = Objects.requireNonNull(id, "Payroll employee id must not be null");
        this.displayName = requireDisplayName(displayName);
        this.active = active;
        this.compensationType = Objects.requireNonNull(compensationType, "Compensation type must not be null");
        this.fixedAmount = fixedAmount;
        this.frequency = frequency;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        validateCompensationShape();
    }

    public static PayrollEmployee createFixed(
            String displayName,
            FinancialAmount fixedAmount,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {
        Objects.requireNonNull(fixedAmount, "Fixed amount must not be null");
        Objects.requireNonNull(effectiveFrom, "Effective from must not be null");
        return new PayrollEmployee(
                UUID.randomUUID(),
                displayName,
                true,
                PayrollCompensationType.FIXED_PAYROLL,
                fixedAmount,
                PayrollFrequency.BIWEEKLY,
                effectiveFrom,
                effectiveTo
        );
    }

    public static PayrollEmployee createProductionBased(String displayName) {
        return new PayrollEmployee(
                UUID.randomUUID(),
                displayName,
                true,
                PayrollCompensationType.PRODUCTION_BASED,
                null,
                null,
                null,
                null
        );
    }

    public static PayrollEmployee reconstitute(
            UUID id,
            String displayName,
            boolean active,
            PayrollCompensationType compensationType,
            FinancialAmount fixedAmount,
            PayrollFrequency frequency,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {
        return new PayrollEmployee(
                id,
                displayName,
                active,
                compensationType,
                fixedAmount,
                frequency,
                effectiveFrom,
                effectiveTo
        );
    }

    /**
     * Actualiza la compensación fija. No muta períodos de nómina ya generados.
     */
    public void updateFixedCompensation(
            FinancialAmount fixedAmount,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {
        if (compensationType != PayrollCompensationType.FIXED_PAYROLL) {
            throw new FinanceDomainException(
                    "Only FIXED_PAYROLL employees can update fixed compensation"
            );
        }
        Objects.requireNonNull(fixedAmount, "Fixed amount must not be null");
        Objects.requireNonNull(effectiveFrom, "Effective from must not be null");
        this.fixedAmount = fixedAmount;
        this.frequency = PayrollFrequency.BIWEEKLY;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        validateCompensationShape();
    }

    public void rename(String displayName) {
        this.displayName = requireDisplayName(displayName);
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean participatesInFixedPayrollGeneration() {
        return active
                && compensationType == PayrollCompensationType.FIXED_PAYROLL
                && fixedAmount != null
                && frequency == PayrollFrequency.BIWEEKLY
                && effectiveFrom != null;
    }

    /**
     * Resuelve períodos biweekly alineados a {@code effectiveFrom} cuyo
     * {@code periodEnd} cae en {@code [fromDate, toDate]}.
     */
    public List<ResolvedPayrollPeriodWindow> resolveBiweeklyPeriodWindows(
            LocalDate fromDate,
            LocalDate toDate
    ) {
        Objects.requireNonNull(fromDate, "From date must not be null");
        Objects.requireNonNull(toDate, "To date must not be null");
        if (!participatesInFixedPayrollGeneration()) {
            return List.of();
        }

        List<ResolvedPayrollPeriodWindow> windows = new ArrayList<>();
        int periodDays = frequency.getPeriodDays();
        LocalDate cursor = effectiveFrom;

        while (!cursor.isAfter(toDate)) {
            LocalDate periodStart = cursor;
            LocalDate periodEnd = cursor.plusDays(periodDays - 1L);

            if (effectiveTo != null && periodStart.isAfter(effectiveTo)) {
                break;
            }

            if (!periodEnd.isBefore(fromDate) && !periodEnd.isAfter(toDate)) {
                LocalDate expectedPaymentDate = PayrollBusinessDayAdjuster.adjustToBusinessDay(periodEnd);
                windows.add(new ResolvedPayrollPeriodWindow(periodStart, periodEnd, expectedPaymentDate));
            }

            cursor = cursor.plusDays(periodDays);
        }

        return List.copyOf(windows);
    }

    public UUID getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isActive() {
        return active;
    }

    public PayrollCompensationType getCompensationType() {
        return compensationType;
    }

    public FinancialAmount getFixedAmount() {
        return fixedAmount;
    }

    public PayrollFrequency getFrequency() {
        return frequency;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        PayrollEmployee that = (PayrollEmployee) other;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private void validateCompensationShape() {
        if (compensationType == PayrollCompensationType.FIXED_PAYROLL) {
            if (fixedAmount == null) {
                throw new FinanceDomainException("FIXED_PAYROLL requires fixed amount");
            }
            if (frequency == null) {
                throw new FinanceDomainException("FIXED_PAYROLL requires frequency");
            }
            if (frequency != PayrollFrequency.BIWEEKLY) {
                throw new FinanceDomainException("FIXED_PAYROLL currently supports only BIWEEKLY frequency");
            }
            if (effectiveFrom == null) {
                throw new FinanceDomainException("FIXED_PAYROLL requires effectiveFrom");
            }
            if (effectiveTo != null && effectiveTo.isBefore(effectiveFrom)) {
                throw new FinanceDomainException("effectiveTo must not be before effectiveFrom");
            }
            return;
        }

        if (fixedAmount != null || frequency != null || effectiveFrom != null || effectiveTo != null) {
            throw new FinanceDomainException(
                    "PRODUCTION_BASED employees must not have fixed payroll compensation fields"
            );
        }
    }

    private static String requireDisplayName(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            throw new FinanceDomainException("Display name must not be blank");
        }
        String normalized = displayName.trim();
        if (normalized.length() > MAX_DISPLAY_NAME_LENGTH) {
            throw new FinanceDomainException(
                    "Display name must not exceed " + MAX_DISPLAY_NAME_LENGTH + " characters"
            );
        }
        return normalized;
    }

    /**
     * Ventana de período resuelta antes de persistir el aggregate {@link PayrollPeriod}.
     */
    public record ResolvedPayrollPeriodWindow(
            LocalDate periodStart,
            LocalDate periodEnd,
            LocalDate expectedPaymentDate
    ) {
    }
}
