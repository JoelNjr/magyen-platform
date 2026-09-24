package com.magyen.platform.commercial.domain;

import com.magyen.platform.commercial.domain.exception.OrderDomainException;
import com.magyen.platform.shared.domain.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderCommercialLifecycleTest {

    private static final LocalDate CONFIRMATION = LocalDate.of(2026, 9, 1);
    private static final LocalDate BUSINESS_TODAY = LocalDate.of(2026, 9, 23);

    @Test
    void confirmedStartsProduction() {
        Order order = confirmedOrder();

        order.startProduction();

        assertEquals(OrderStatus.IN_PRODUCTION, order.getStatus());
    }

    @Test
    void startProductionRejectsEveryOtherStatus() {
        Order inProduction = confirmedOrder();
        inProduction.startProduction();
        assertStatusUnchanged(inProduction, inProduction::startProduction);

        Order ready = confirmedOrder();
        ready.startProduction();
        ready.markReadyForDelivery();
        assertStatusUnchanged(ready, ready::startProduction);

        Order delivered = deliveredOrder();
        assertStatusUnchanged(delivered, delivered::startProduction);

        Order closed = closedOrder();
        assertStatusUnchanged(closed, closed::startProduction);
    }

    @Test
    void inProductionBecomesReadyForDelivery() {
        Order order = confirmedOrder();
        order.startProduction();

        order.markReadyForDelivery();

        assertEquals(OrderStatus.READY_FOR_DELIVERY, order.getStatus());
    }

    @Test
    void readyForDeliveryRejectsEveryOtherStatus() {
        Order confirmed = confirmedOrder();
        assertStatusUnchanged(confirmed, confirmed::markReadyForDelivery);

        Order ready = confirmedOrder();
        ready.startProduction();
        ready.markReadyForDelivery();
        assertStatusUnchanged(ready, ready::markReadyForDelivery);

        Order delivered = deliveredOrder();
        assertStatusUnchanged(delivered, delivered::markReadyForDelivery);

        Order closed = closedOrder();
        assertStatusUnchanged(closed, closed::markReadyForDelivery);
    }

    @Test
    void deliveredClosesAfterFinalPaymentAcknowledgment() {
        Order order = deliveredOrder();
        assertFalse(order.getPaymentSummary().isFinalPaymentAcknowledged());

        order.acknowledgeFinalPayment();
        order.close();

        assertEquals(OrderStatus.CLOSED, order.getStatus());
        assertTrue(order.getPaymentSummary().isFinalPaymentAcknowledged());
    }

    @Test
    void closeBeforeDeliveryIsRejected() {
        Order confirmed = confirmedOrder();
        confirmed.acknowledgeFinalPayment();
        assertStatusUnchanged(confirmed, confirmed::close);

        Order ready = confirmedOrder();
        ready.startProduction();
        ready.markReadyForDelivery();
        ready.acknowledgeFinalPayment();
        assertStatusUnchanged(ready, ready::close);
    }

    @Test
    void closeWithoutFinalPaymentAcknowledgmentIsRejected() {
        Order order = deliveredOrder();

        OrderDomainException exception = assertThrows(OrderDomainException.class, order::close);

        assertTrue(exception.getMessage().contains("final payment acknowledgment"));
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
    }

    private static void assertStatusUnchanged(Order order, Runnable action) {
        OrderStatus before = order.getStatus();
        assertThrows(OrderDomainException.class, action::run);
        assertEquals(before, order.getStatus());
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
                OrderNumber.of("ORD-LC-" + UUID.randomUUID().toString().substring(0, 8)),
                UUID.randomUUID(),
                UUID.randomUUID(),
                CONFIRMATION,
                DeliveryCommitment.of(CONFIRMATION.plusDays(10)),
                UUID.randomUUID(),
                null,
                List.of(item)
        );
    }

    private static Order deliveredOrder() {
        Order order = confirmedOrder();
        order.startProduction();
        order.markReadyForDelivery();
        order.deliver(LocalDate.of(2026, 9, 18), BUSINESS_TODAY);
        return order;
    }

    private static Order closedOrder() {
        Order order = deliveredOrder();
        order.acknowledgeFinalPayment();
        order.close();
        return order;
    }
}
