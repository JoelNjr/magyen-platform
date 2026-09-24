package com.magyen.platform.commercial.domain;

import com.magyen.platform.shared.domain.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderIndividualProfitabilityMonthPolicyTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 14);
    private static final LocalDate SEPTEMBER_START = LocalDate.of(2026, 9, 1);
    private static final LocalDate SEPTEMBER_END = LocalDate.of(2026, 9, 30);
    private static final LocalDate AUGUST_START = LocalDate.of(2026, 8, 1);
    private static final LocalDate AUGUST_END = LocalDate.of(2026, 8, 31);
    private static final LocalDate OCTOBER_START = LocalDate.of(2026, 10, 1);
    private static final LocalDate OCTOBER_END = LocalDate.of(2026, 10, 31);

    @Test
    void deliveredWithActualDateInSelectedMonthIsIncluded() {
        Order order = delivered(LocalDate.of(2026, 9, 18), LocalDate.of(2026, 8, 20));
        assertTrue(OrderIndividualProfitabilityMonthPolicy.includes(
                order, SEPTEMBER_START, SEPTEMBER_END, TODAY
        ));
    }

    @Test
    void deliveredWithActualDateOutsideSelectedMonthIsExcluded() {
        Order order = delivered(LocalDate.of(2026, 10, 2), LocalDate.of(2026, 9, 14));
        assertFalse(OrderIndividualProfitabilityMonthPolicy.includes(
                order, SEPTEMBER_START, SEPTEMBER_END, TODAY
        ));
    }

    @Test
    void deliveredWithoutActualDateUsesPromisedDateFallback() {
        Order order = delivered(null, LocalDate.of(2026, 9, 14));
        assertTrue(OrderIndividualProfitabilityMonthPolicy.includes(
                order, SEPTEMBER_START, SEPTEMBER_END, TODAY
        ));
        assertEquals(
                OrderProfitabilityDeliveryDateSource.HISTORICAL_FALLBACK,
                OrderIndividualProfitabilityMonthPolicy.deliveryDateSource(order)
        );
    }

    @Test
    void deliveredWithoutActualDateOutsidePromisedMonthIsExcluded() {
        Order order = delivered(null, LocalDate.of(2026, 8, 20));
        assertFalse(OrderIndividualProfitabilityMonthPolicy.includes(
                order, SEPTEMBER_START, SEPTEMBER_END, TODAY
        ));
    }

    @Test
    void actualDateIgnoresPromisedDateForMembership() {
        Order order = delivered(LocalDate.of(2026, 10, 2), LocalDate.of(2026, 9, 14));
        assertFalse(OrderIndividualProfitabilityMonthPolicy.includes(
                order, SEPTEMBER_START, SEPTEMBER_END, TODAY
        ));
        assertTrue(OrderIndividualProfitabilityMonthPolicy.includes(
                order, OCTOBER_START, OCTOBER_END, TODAY
        ));
        assertEquals(
                OrderProfitabilityDeliveryDateSource.ACTUAL,
                OrderIndividualProfitabilityMonthPolicy.deliveryDateSource(order)
        );
    }

    @Test
    void confirmedCurrentMonthIsIncluded() {
        Order order = withStatus(OrderStatus.CONFIRMED);
        assertTrue(OrderIndividualProfitabilityMonthPolicy.includes(
                order, SEPTEMBER_START, SEPTEMBER_END, TODAY
        ));
    }

    @Test
    void inProductionCurrentMonthIsIncluded() {
        Order order = withStatus(OrderStatus.IN_PRODUCTION);
        assertTrue(OrderIndividualProfitabilityMonthPolicy.includes(
                order, SEPTEMBER_START, SEPTEMBER_END, TODAY
        ));
    }

    @Test
    void readyForDeliveryCurrentMonthIsIncluded() {
        Order order = withStatus(OrderStatus.READY_FOR_DELIVERY);
        assertTrue(OrderIndividualProfitabilityMonthPolicy.includes(
                order, SEPTEMBER_START, SEPTEMBER_END, TODAY
        ));
    }

    @Test
    void undeliveredOrderIsExcludedFromHistoricalMonths() {
        Order order = withStatus(OrderStatus.IN_PRODUCTION);
        assertFalse(OrderIndividualProfitabilityMonthPolicy.includes(
                order, AUGUST_START, AUGUST_END, TODAY
        ));
    }

    @Test
    void futureMonthContainsNoCurrentWip() {
        Order order = withStatus(OrderStatus.CONFIRMED);
        assertFalse(OrderIndividualProfitabilityMonthPolicy.includes(
                order, OCTOBER_START, OCTOBER_END, TODAY
        ));
    }

    @Test
    void closedIsExcluded() {
        Order order = withStatus(OrderStatus.CLOSED);
        assertFalse(OrderIndividualProfitabilityMonthPolicy.includes(
                order, SEPTEMBER_START, SEPTEMBER_END, TODAY
        ));
        assertFalse(OrderProfitabilityEligibility.includes(order.getStatus()));
    }

    private static Order delivered(LocalDate actualDeliveryDate, LocalDate promisedDeliveryDate) {
        Order created = base(promisedDeliveryDate);
        return Order.reconstitute(
                created.getId(),
                created.getOrderNumber(),
                created.getCustomerId(),
                created.getQuotationId(),
                created.getConfirmationDate(),
                OrderStatus.DELIVERED,
                created.getDeliveryCommitment(),
                created.getPaymentSummary(),
                created.getSellerId(),
                created.getObservations(),
                created.getDescription(),
                created.getItems(),
                created.getDiscount(),
                actualDeliveryDate
        );
    }

    private static Order withStatus(OrderStatus status) {
        Order created = base(LocalDate.of(2026, 9, 25));
        if (status == OrderStatus.CONFIRMED) {
            return created;
        }
        PaymentSummary payment = status == OrderStatus.CLOSED
                ? PaymentSummary.of(true, true, created.getTotal())
                : created.getPaymentSummary();
        return Order.reconstitute(
                created.getId(),
                created.getOrderNumber(),
                created.getCustomerId(),
                created.getQuotationId(),
                created.getConfirmationDate(),
                status,
                created.getDeliveryCommitment(),
                payment,
                created.getSellerId(),
                created.getObservations(),
                created.getDescription(),
                created.getItems(),
                created.getDiscount(),
                null
        );
    }

    private static Order base(LocalDate promisedDeliveryDate) {
        OrderItem item = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Camiseta",
                1,
                "Algodón",
                "Blanco",
                Money.of(new BigDecimal("100000.00")),
                ProductSpecification.empty(),
                List.of()
        );
        return Order.create(
                OrderNumber.of("ORD-POL-" + UUID.randomUUID().toString().substring(0, 8)),
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.of(2026, 8, 20),
                DeliveryCommitment.of(promisedDeliveryDate),
                UUID.randomUUID(),
                null,
                List.of(item)
        );
    }
}
