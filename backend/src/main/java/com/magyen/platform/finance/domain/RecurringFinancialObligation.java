package com.magyen.platform.finance.domain;

import com.magyen.platform.finance.domain.exception.FinanceDomainException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root de una obligación financiera recurrente o fija.
 * <p>
 * Representa un compromiso esperado o pendiente. Su existencia NO crea
 * {@link FinancialTransaction}; el pago explícito quedará para un incremento futuro.
 */
public class RecurringFinancialObligation {

    private static final int MAX_NAME_LENGTH = 255;
    private static final int MAX_TEXT_LENGTH = 2000;

    private final UUID id;
    private String name;
    private RecurringObligationType type;
    private FinancialAmount expectedAmount;
    private RecurringObligationFrequency frequency;
    private Integer dueDay;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
    private String description;
    private String observation;

    private RecurringFinancialObligation(
            UUID id,
            String name,
            RecurringObligationType type,
            FinancialAmount expectedAmount,
            RecurringObligationFrequency frequency,
            Integer dueDay,
            LocalDate startDate,
            LocalDate endDate,
            boolean active,
            String description,
            String observation
    ) {
        this.id = Objects.requireNonNull(id, "Obligation id must not be null");
        applyMutableState(
                name,
                type,
                expectedAmount,
                frequency,
                dueDay,
                startDate,
                endDate,
                description,
                observation
        );
        this.active = active;
    }

    /**
     * Crea una obligación recurrente activa.
     * <p>
     * No genera movimientos del ledger ni registros de pago.
     */
    public static RecurringFinancialObligation create(
            String name,
            RecurringObligationType type,
            FinancialAmount expectedAmount,
            RecurringObligationFrequency frequency,
            Integer dueDay,
            LocalDate startDate,
            LocalDate endDate,
            String description,
            String observation
    ) {
        return new RecurringFinancialObligation(
                UUID.randomUUID(),
                name,
                type,
                expectedAmount,
                frequency,
                dueDay,
                startDate,
                endDate,
                true,
                description,
                observation
        );
    }

    /**
     * Reconstruye desde persistencia. No aplica lógica de creación de negocio.
     */
    public static RecurringFinancialObligation reconstitute(
            UUID id,
            String name,
            RecurringObligationType type,
            FinancialAmount expectedAmount,
            RecurringObligationFrequency frequency,
            Integer dueDay,
            LocalDate startDate,
            LocalDate endDate,
            boolean active,
            String description,
            String observation
    ) {
        return new RecurringFinancialObligation(
                id,
                name,
                type,
                expectedAmount,
                frequency,
                dueDay,
                startDate,
                endDate,
                active,
                description,
                observation
        );
    }

    /**
     * Actualiza los datos de la obligación sin alterar su estado activo.
     */
    public void update(
            String name,
            RecurringObligationType type,
            FinancialAmount expectedAmount,
            RecurringObligationFrequency frequency,
            Integer dueDay,
            LocalDate startDate,
            LocalDate endDate,
            String description,
            String observation
    ) {
        applyMutableState(
                name,
                type,
                expectedAmount,
                frequency,
                dueDay,
                startDate,
                endDate,
                description,
                observation
        );
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    /**
     * Calcula las fechas de vencimiento compatibles dentro de {@code [fromDate, toDate]}.
     * <p>
     * Respeta frecuencia, dueDay, startDate y endDate. No persiste ni crea ocurrencias.
     * <p>
     * Convención mensual/anual cuando {@code dueDay} supera los días del mes:
     * se usa el último día del mes ({@code Math.min(dueDay, lengthOfMonth)}),
     * la misma regla ya aplicada por {@link #requireCompatibleOccurrenceDueDate(LocalDate)}.
     * <p>
     * Si {@code dueDay} es nulo, se ancla en el día/mes/semana implícitos de {@code startDate}.
     */
    public List<LocalDate> resolveOccurrenceDueDates(LocalDate fromDate, LocalDate toDate) {
        Objects.requireNonNull(fromDate, "From date must not be null");
        Objects.requireNonNull(toDate, "To date must not be null");
        if (toDate.isBefore(fromDate)) {
            throw new FinanceDomainException("To date must not precede from date");
        }

        LocalDate rangeStart = fromDate.isBefore(startDate) ? startDate : fromDate;
        LocalDate rangeEnd = endDate != null && endDate.isBefore(toDate) ? endDate : toDate;
        if (rangeStart.isAfter(rangeEnd)) {
            return List.of();
        }

        List<LocalDate> dueDates = switch (frequency) {
            case MONTHLY -> resolveMonthlyDueDates(rangeStart, rangeEnd);
            case YEARLY -> resolveYearlyDueDates(rangeStart, rangeEnd);
            case WEEKLY -> resolveWeeklyDueDates(rangeStart, rangeEnd);
            case BIWEEKLY -> resolveBiweeklyDueDates(rangeStart, rangeEnd);
        };

        return dueDates.stream()
                .filter(date -> !date.isBefore(fromDate) && !date.isAfter(toDate))
                .filter(date -> {
                    try {
                        requireCompatibleOccurrenceDueDate(date);
                        return true;
                    } catch (FinanceDomainException exception) {
                        return false;
                    }
                })
                .distinct()
                .sorted()
                .toList();
    }

    /**
     * Valida que una fecha de ocurrencia sea compatible con el rango y la regla de vencimiento.
     */
    public void requireCompatibleOccurrenceDueDate(LocalDate dueDate) {
        Objects.requireNonNull(dueDate, "Due date must not be null");

        if (dueDate.isBefore(startDate)) {
            throw new FinanceDomainException("Occurrence due date must not precede obligation start date");
        }
        if (endDate != null && dueDate.isAfter(endDate)) {
            throw new FinanceDomainException("Occurrence due date must not be after obligation end date");
        }

        if (dueDay == null) {
            return;
        }

        switch (frequency) {
            case MONTHLY -> requireMonthlyDueDayMatch(dueDate);
            case YEARLY -> {
                if (dueDate.getMonth() != startDate.getMonth()) {
                    throw new FinanceDomainException(
                            "Yearly occurrence due date must fall in month " + startDate.getMonth()
                    );
                }
                requireMonthlyDueDayMatch(dueDate);
            }
            case WEEKLY -> {
                if (dueDate.getDayOfWeek().getValue() != dueDay) {
                    throw new FinanceDomainException(
                            "Weekly occurrence due date must fall on day of week " + dueDay
                    );
                }
            }
            case BIWEEKLY -> {
                if (dueDay <= 7) {
                    if (dueDate.getDayOfWeek().getValue() != dueDay) {
                        throw new FinanceDomainException(
                                "Biweekly occurrence due date must fall on day of week " + dueDay
                        );
                    }
                } else {
                    long daysFromStart = java.time.temporal.ChronoUnit.DAYS.between(startDate, dueDate);
                    int dayInCycle = (int) (daysFromStart % 14) + 1;
                    if (dayInCycle != dueDay) {
                        throw new FinanceDomainException(
                                "Biweekly occurrence due date must match due day " + dueDay
                                        + " within the biweekly cycle"
                        );
                    }
                }
            }
        }
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public RecurringObligationType getType() {
        return type;
    }

    public FinancialAmount getExpectedAmount() {
        return expectedAmount;
    }

    public RecurringObligationFrequency getFrequency() {
        return frequency;
    }

    public Integer getDueDay() {
        return dueDay;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public boolean isActive() {
        return active;
    }

    public String getDescription() {
        return description;
    }

    public String getObservation() {
        return observation;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        RecurringFinancialObligation that = (RecurringFinancialObligation) other;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private void applyMutableState(
            String name,
            RecurringObligationType type,
            FinancialAmount expectedAmount,
            RecurringObligationFrequency frequency,
            Integer dueDay,
            LocalDate startDate,
            LocalDate endDate,
            String description,
            String observation
    ) {
        this.name = requireName(name);
        this.type = Objects.requireNonNull(type, "Obligation type must not be null");
        this.expectedAmount = Objects.requireNonNull(expectedAmount, "Expected amount must not be null");
        this.frequency = Objects.requireNonNull(frequency, "Frequency must not be null");
        this.startDate = Objects.requireNonNull(startDate, "Start date must not be null");
        this.frequency.requireValidDueDay(dueDay);
        this.dueDay = dueDay;
        this.endDate = requireValidEndDate(startDate, endDate);
        this.description = normalizeOptionalText(description, "Description");
        this.observation = normalizeOptionalText(observation, "Observation");
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new FinanceDomainException("Obligation name must not be blank");
        }
        String normalized = name.trim();
        if (normalized.length() > MAX_NAME_LENGTH) {
            throw new FinanceDomainException("Obligation name must not exceed " + MAX_NAME_LENGTH + " characters");
        }
        return normalized;
    }

    private static LocalDate requireValidEndDate(LocalDate startDate, LocalDate endDate) {
        if (endDate == null) {
            return null;
        }
        if (endDate.isBefore(startDate)) {
            throw new FinanceDomainException("End date must not precede start date");
        }
        return endDate;
    }

    private void requireMonthlyDueDayMatch(LocalDate dueDate) {
        int lengthOfMonth = dueDate.lengthOfMonth();
        int effectiveDueDay = Math.min(resolveMonthlyDayAnchor(), lengthOfMonth);
        if (dueDate.getDayOfMonth() != effectiveDueDay) {
            throw new FinanceDomainException(
                    "Occurrence due date must match obligation due day " + resolveMonthlyDayAnchor()
            );
        }
    }

    private List<LocalDate> resolveMonthlyDueDates(LocalDate rangeStart, LocalDate rangeEnd) {
        List<LocalDate> dates = new ArrayList<>();
        YearMonth cursor = YearMonth.from(rangeStart);
        YearMonth last = YearMonth.from(rangeEnd);
        int dayAnchor = resolveMonthlyDayAnchor();

        while (!cursor.isAfter(last)) {
            int day = Math.min(dayAnchor, cursor.lengthOfMonth());
            LocalDate candidate = cursor.atDay(day);
            if (!candidate.isBefore(rangeStart) && !candidate.isAfter(rangeEnd)) {
                dates.add(candidate);
            }
            cursor = cursor.plusMonths(1);
        }
        return dates;
    }

    private List<LocalDate> resolveYearlyDueDates(LocalDate rangeStart, LocalDate rangeEnd) {
        List<LocalDate> dates = new ArrayList<>();
        int dayAnchor = resolveMonthlyDayAnchor();
        int year = rangeStart.getYear();
        int lastYear = rangeEnd.getYear();

        while (year <= lastYear) {
            YearMonth anniversaryMonth = YearMonth.of(year, startDate.getMonth());
            int day = Math.min(dayAnchor, anniversaryMonth.lengthOfMonth());
            LocalDate candidate = anniversaryMonth.atDay(day);
            if (!candidate.isBefore(rangeStart) && !candidate.isAfter(rangeEnd)) {
                dates.add(candidate);
            }
            year++;
        }
        return dates;
    }

    private List<LocalDate> resolveWeeklyDueDates(LocalDate rangeStart, LocalDate rangeEnd) {
        int targetDayOfWeek = dueDay != null ? dueDay : startDate.getDayOfWeek().getValue();
        LocalDate cursor = rangeStart;
        while (cursor.getDayOfWeek().getValue() != targetDayOfWeek) {
            cursor = cursor.plusDays(1);
            if (cursor.isAfter(rangeEnd)) {
                return List.of();
            }
        }

        List<LocalDate> dates = new ArrayList<>();
        while (!cursor.isAfter(rangeEnd)) {
            dates.add(cursor);
            cursor = cursor.plusWeeks(1);
        }
        return dates;
    }

    private List<LocalDate> resolveBiweeklyDueDates(LocalDate rangeStart, LocalDate rangeEnd) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate cursor;

        if (dueDay != null && dueDay > 7) {
            cursor = startDate.plusDays(dueDay - 1L);
        } else {
            int targetDayOfWeek = dueDay != null ? dueDay : startDate.getDayOfWeek().getValue();
            cursor = startDate;
            while (cursor.getDayOfWeek().getValue() != targetDayOfWeek) {
                cursor = cursor.plusDays(1);
            }
        }

        while (cursor.isBefore(rangeStart)) {
            cursor = cursor.plusDays(14);
        }

        while (!cursor.isAfter(rangeEnd)) {
            dates.add(cursor);
            cursor = cursor.plusDays(14);
        }
        return dates;
    }

    private int resolveMonthlyDayAnchor() {
        return dueDay != null ? dueDay : startDate.getDayOfMonth();
    }

    private static String normalizeOptionalText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > MAX_TEXT_LENGTH) {
            throw new FinanceDomainException(fieldName + " must not exceed " + MAX_TEXT_LENGTH + " characters");
        }
        return normalized;
    }
}
