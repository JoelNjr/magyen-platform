package com.magyen.platform.finance.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Ajusta una fecha ancla de nómina al día hábil válido.
 * <p>
 * Regla determinística (sin festivos colombianos):
 * <ul>
 *   <li>Lunes a viernes → sin cambio</li>
 *   <li>Sábado → viernes anterior</li>
 *   <li>Domingo → lunes siguiente</li>
 * </ul>
 * Festivos públicos quedan diferidos a un calendario futuro.
 */
public final class PayrollBusinessDayAdjuster {

    private PayrollBusinessDayAdjuster() {
    }

    public static LocalDate adjustToBusinessDay(LocalDate date) {
        Objects.requireNonNull(date, "Date must not be null");
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY) {
            return date.minusDays(1);
        }
        if (dayOfWeek == DayOfWeek.SUNDAY) {
            return date.plusDays(1);
        }
        return date;
    }
}
