package com.magyen.platform.finance.domain;

import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;
import java.util.UUID;

/**
 * Semana de agrupación de pagos de mano de obra en el ledger.
 * <p>
 * Convención: lunes a domingo (ISO-8601). No existía una definición de semana
 * para este flujo; la nómina fija es quincenal y las obligaciones recurrentes
 * numeran el lunes como día 1. Se adopta la misma semana ISO.
 * <p>
 * La identidad del movimiento semanal es estable: {@code sourceId} se deriva
 * del lunes de la semana de la fecha real de pago.
 */
public final class LaborPaymentWeek {

    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String SOURCE_PREFIX = "PRODUCTION_LABOR_WEEK:";

    private final LocalDate weekStart;
    private final LocalDate weekEnd;

    private LaborPaymentWeek(LocalDate weekStart) {
        this.weekStart = weekStart;
        this.weekEnd = weekStart.plusDays(6);
    }

    public static LaborPaymentWeek of(LocalDate paymentDate) {
        Objects.requireNonNull(paymentDate, "Payment date must not be null");
        LocalDate weekStart = paymentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return new LaborPaymentWeek(weekStart);
    }

    public LocalDate getWeekStart() {
        return weekStart;
    }

    public LocalDate getWeekEnd() {
        return weekEnd;
    }

    /**
     * Identidad estable del movimiento semanal. Compatible con
     * {@code uq_financial_transactions_payroll_source}.
     */
    public UUID sourceId() {
        return UUID.nameUUIDFromBytes((SOURCE_PREFIX + weekStart).getBytes(StandardCharsets.UTF_8));
    }

    public String description(int paymentCount) {
        if (paymentCount < 1) {
            throw new IllegalArgumentException("Payment count must be greater than zero");
        }
        String paymentLabel = paymentCount == 1 ? "1 pago" : paymentCount + " pagos";
        return "Mano de obra — Semana del " + weekStart.format(DISPLAY_DATE)
                + " (" + paymentLabel + ")";
    }

    public String observation(int paymentCount) {
        return "pagos=" + paymentCount;
    }

    public static int parsePaymentCount(String observation) {
        if (observation == null || observation.isBlank()) {
            return 1;
        }
        String normalized = observation.trim();
        if (!normalized.startsWith("pagos=")) {
            return 1;
        }
        try {
            int count = Integer.parseInt(normalized.substring("pagos=".length()));
            return count < 1 ? 1 : count;
        } catch (NumberFormatException exception) {
            return 1;
        }
    }
}
