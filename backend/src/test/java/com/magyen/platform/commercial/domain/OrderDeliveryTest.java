package com.magyen.platform.commercial.domain;

import com.magyen.platform.commercial.domain.exception.OrderDomainException;
import com.magyen.platform.shared.domain.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderDeliveryTest {

    private static final LocalDate CONFIRMATION = LocalDate.of(2026, 9, 1);
    private static final LocalDate BUSINESS_TODAY = LocalDate.of(2026, 9, 23);

    @Test
    void createHasNullActualDeliveryDate() {
        Order order = confirmedOrder();
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertNull(order.getActualDeliveryDate());
    }

    @Test
    void validDeliverSetsDeliveredAndDate() {
        Order order = readyForDelivery();
        LocalDate actual = LocalDate.of(2026, 9, 18);

        order.deliver(actual, BUSINESS_TODAY);

        assertEquals(OrderStatus.DELIVERED, order.getStatus());
        assertEquals(actual, order.getActualDeliveryDate());
    }

    @Test
    void nullActualDeliveryDateIsRejected() {
        Order order = readyForDelivery();

        assertThrows(NullPointerException.class, () -> order.deliver(null, BUSINESS_TODAY));
        assertEquals(OrderStatus.READY_FOR_DELIVERY, order.getStatus());
        assertNull(order.getActualDeliveryDate());
    }

    @Test
    void dateBeforeConfirmationIsRejected() {
        Order order = readyForDelivery();

        OrderDomainException exception = assertThrows(
                OrderDomainException.class,
                () -> order.deliver(LocalDate.of(2026, 8, 31), BUSINESS_TODAY)
        );

        assertTrue(exception.getMessage().contains("confirmation date"));
        assertEquals(OrderStatus.READY_FOR_DELIVERY, order.getStatus());
        assertNull(order.getActualDeliveryDate());
    }

    @Test
    void confirmedCannotBeDelivered() {
        Order order = confirmedOrder();

        OrderDomainException exception = assertThrows(
                OrderDomainException.class,
                () -> order.deliver(LocalDate.of(2026, 9, 18), BUSINESS_TODAY)
        );

        assertTrue(exception.getMessage().contains("Invalid order status transition"));
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertNull(order.getActualDeliveryDate());
    }

    @Test
    void inProductionCannotBeDelivered() {
        Order order = confirmedOrder();
        order.startProduction();

        OrderDomainException exception = assertThrows(
                OrderDomainException.class,
                () -> order.deliver(LocalDate.of(2026, 9, 18), BUSINESS_TODAY)
        );

        assertEquals(OrderStatus.IN_PRODUCTION, order.getStatus());
        assertNull(order.getActualDeliveryDate());
        assertTrue(exception.getMessage().contains("Invalid order status transition"));
    }

    @Test
    void secondDeliverFromDeliveredIsRejectedAndKeepsTheDate() {
        Order order = readyForDelivery();
        LocalDate first = LocalDate.of(2026, 9, 18);
        order.deliver(first, BUSINESS_TODAY);

        OrderDomainException exception = assertThrows(
                OrderDomainException.class,
                () -> order.deliver(LocalDate.of(2026, 9, 20), BUSINESS_TODAY)
        );

        assertTrue(exception.getMessage().contains("Invalid order status transition"));
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
        assertEquals(first, order.getActualDeliveryDate());
    }

    @Test
    void dateAfterBusinessTodayIsRejected() {
        Order order = readyForDelivery();

        OrderDomainException exception = assertThrows(
                OrderDomainException.class,
                () -> order.deliver(BUSINESS_TODAY.plusDays(1), BUSINESS_TODAY)
        );

        assertTrue(exception.getMessage().contains("current business date"));
        assertEquals(OrderStatus.READY_FOR_DELIVERY, order.getStatus());
        assertNull(order.getActualDeliveryDate());
    }

    @Test
    void nullBusinessTodayIsRejected() {
        Order order = readyForDelivery();

        assertThrows(
                NullPointerException.class,
                () -> order.deliver(LocalDate.of(2026, 9, 18), null)
        );
        assertEquals(OrderStatus.READY_FOR_DELIVERY, order.getStatus());
    }

    @Test
    void reconstituteNullActualDeliveryDateWorks() {
        Order created = confirmedOrder();
        Order reconstituted = Order.reconstitute(
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
                null
        );

        assertEquals(OrderStatus.DELIVERED, reconstituted.getStatus());
        assertNull(reconstituted.getActualDeliveryDate());
    }

    @Test
    void reconstituteActualDeliveryDateWorks() {
        Order created = confirmedOrder();
        LocalDate actual = LocalDate.of(2026, 9, 18);
        Order reconstituted = Order.reconstitute(
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
                actual
        );

        assertEquals(OrderStatus.DELIVERED, reconstituted.getStatus());
        assertEquals(actual, reconstituted.getActualDeliveryDate());
    }

    private static Order confirmedOrder() {
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
                OrderNumber.of("ORD-DEL-" + UUID.randomUUID().toString().substring(0, 8)),
                UUID.randomUUID(),
                UUID.randomUUID(),
                CONFIRMATION,
                DeliveryCommitment.of(CONFIRMATION.plusDays(10)),
                UUID.randomUUID(),
                null,
                List.of(item)
        );
    }

    private static Order readyForDelivery() {
        Order order = confirmedOrder();
        order.startProduction();
        order.markReadyForDelivery();
        return order;
    }
}
