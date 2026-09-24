package com.magyen.platform.commercial.domain;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Membresía mensual de rentabilidad individual (no Home).
 * <p>
 * DELIVERED con fecha real → {@code actualDeliveryDate}.
 * DELIVERED histórico sin fecha real → fallback de {@code promisedDeliveryDate} solo para selección.
 * No entregados elegibles → mes calendario actual.
 * CLOSED queda fuera vía {@link OrderProfitabilityEligibility}.
 */
public final class OrderIndividualProfitabilityMonthPolicy {

    private OrderIndividualProfitabilityMonthPolicy() {
    }

    public static boolean includes(Order order, LocalDate fromDate, LocalDate toDate, LocalDate today) {
        Objects.requireNonNull(order, "Order must not be null");
        Objects.requireNonNull(fromDate, "From date must not be null");
        Objects.requireNonNull(toDate, "To date must not be null");
        Objects.requireNonNull(today, "Today must not be null");

        if (!OrderProfitabilityEligibility.includes(order.getStatus())) {
            return false;
        }

        if (order.getStatus() == OrderStatus.DELIVERED) {
            LocalDate classificationDate = classificationDate(order);
            return inRange(classificationDate, fromDate, toDate);
        }

        return isCurrentCalendarMonth(fromDate, toDate, today);
    }

    /**
     * Fecha que clasifica el pedido en el mes. No escribe ni inventa {@code actualDeliveryDate}.
     */
    public static LocalDate classificationDate(Order order) {
        Objects.requireNonNull(order, "Order must not be null");
        if (order.getStatus() == OrderStatus.DELIVERED && order.getActualDeliveryDate() != null) {
            return order.getActualDeliveryDate();
        }
        return order.getDeliveryCommitment().getPromisedDeliveryDate();
    }

    public static OrderProfitabilityDeliveryDateSource deliveryDateSource(Order order) {
        Objects.requireNonNull(order, "Order must not be null");
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CLOSED) {
            return order.getActualDeliveryDate() != null
                    ? OrderProfitabilityDeliveryDateSource.ACTUAL
                    : OrderProfitabilityDeliveryDateSource.HISTORICAL_FALLBACK;
        }
        return OrderProfitabilityDeliveryDateSource.CURRENT_MONTH;
    }

    public static boolean isCurrentCalendarMonth(LocalDate fromDate, LocalDate toDate, LocalDate today) {
        Objects.requireNonNull(fromDate, "From date must not be null");
        Objects.requireNonNull(toDate, "To date must not be null");
        Objects.requireNonNull(today, "Today must not be null");
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());
        return fromDate.equals(monthStart) && toDate.equals(monthEnd);
    }

    private static boolean inRange(LocalDate date, LocalDate fromDate, LocalDate toDate) {
        return date != null && !date.isBefore(fromDate) && !date.isAfter(toDate);
    }
}
