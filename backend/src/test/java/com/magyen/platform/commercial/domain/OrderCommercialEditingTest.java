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

class OrderCommercialEditingTest {

    @Test
    void confirmedInProductionAndReadyForDeliveryRemainEditable() {
        for (OrderStatus status : List.of(
                OrderStatus.CONFIRMED,
                OrderStatus.IN_PRODUCTION,
                OrderStatus.READY_FOR_DELIVERY
        )) {
            Order order = orderWithStatus(status, item("Camiseta", 10, "20000"));
            String originalNumber = order.getOrderNumber().getValue();

            order.updateItemCommercialCommitment(
                    order.getItems().getFirst().getId(),
                    12,
                    Money.of(new BigDecimal("20000"))
            );

            assertEquals(status, order.getStatus());
            assertEquals(originalNumber, order.getOrderNumber().getValue());
            assertEquals(12, order.getItems().getFirst().getQuantity());
            assertEquals(Money.of(new BigDecimal("240000")), order.getTotal());
        }
    }

    @Test
    void deliveredAndClosedRejectCommercialEdits() {
        for (OrderStatus status : List.of(OrderStatus.DELIVERED, OrderStatus.CLOSED)) {
            Order order = orderWithStatus(status, item("Camiseta", 10, "20000"));
            UUID itemId = order.getItems().getFirst().getId();

            OrderDomainException exception = assertThrows(
                    OrderDomainException.class,
                    () -> order.updateItemCommercialCommitment(itemId, 12, Money.of(new BigDecimal("20000")))
            );
            assertTrue(exception.getMessage().contains("CONFIRMED, IN_PRODUCTION or READY_FOR_DELIVERY"));
            assertEquals(10, order.getItems().getFirst().getQuantity());
        }
    }

    @Test
    void decreaseQuantityIsRejectedWhenSizesWouldExceedAndSizesStayIntact() {
        UUID quotationItemId = UUID.randomUUID();
        OrderItem traced = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Camiseta",
                20,
                "Sudáfrica",
                "Blanco",
                Money.of(new BigDecimal("45000")),
                ProductSpecification.empty(),
                List.of(
                        SizeBreakdown.create("S", 5),
                        SizeBreakdown.create("M", 10),
                        SizeBreakdown.create("L", 5)
                ),
                quotationItemId
        );
        Order order = confirmedOrder(List.of(traced));

        OrderDomainException exception = assertThrows(
                OrderDomainException.class,
                () -> order.updateItemCommercialCommitment(
                        traced.getId(),
                        18,
                        Money.of(new BigDecimal("45000"))
                )
        );

        assertTrue(exception.getMessage().contains("Assigned: 20"));
        assertEquals(20, order.getItems().getFirst().getQuantity());
        assertEquals(20, order.getItems().getFirst().getAssignedSizeQuantity());
        assertEquals(quotationItemId, order.getItems().getFirst().getQuotationItemId());
        assertEquals(Money.of(new BigDecimal("900000")), order.getTotal());
    }

    @Test
    void decreaseQuantitySucceedsWhenSizesStillFitAndTraceabilityStays() {
        UUID quotationItemId = UUID.randomUUID();
        OrderItem traced = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Camiseta",
                20,
                "Sudáfrica",
                "Blanco",
                Money.of(new BigDecimal("45000")),
                ProductSpecification.empty(),
                List.of(SizeBreakdown.create("S", 5), SizeBreakdown.create("M", 5)),
                quotationItemId
        );
        Order order = confirmedOrder(List.of(traced));

        order.updateItemCommercialCommitment(
                traced.getId(),
                12,
                Money.of(new BigDecimal("50000"))
        );

        assertEquals(12, order.getItems().getFirst().getQuantity());
        assertEquals(Money.of(new BigDecimal("50000")), order.getItems().getFirst().getUnitPrice());
        assertEquals(10, order.getItems().getFirst().getAssignedSizeQuantity());
        assertEquals(quotationItemId, order.getItems().getFirst().getQuotationItemId());
        assertEquals(Money.of(new BigDecimal("600000")), order.getTotal());
    }

    @Test
    void manuallyAddedItemHasNullQuotationItemIdAndRecalculatesTotal() {
        Order order = confirmedOrder(List.of(item("Camiseta", 10, "20000")));

        order.addItem(
                "Pantaloneta",
                4,
                "Hydrotech",
                null,
                "Negro",
                Money.of(new BigDecimal("15000")),
                ProductSpecification.empty()
        );

        assertEquals(2, order.getItems().size());
        assertNull(order.getItems().get(1).getQuotationItemId());
        assertEquals(Money.of(new BigDecimal("260000")), order.getTotal());
        assertEquals(
                order.getSubtotal().subtract(order.getDiscount()),
                order.getTotal()
        );
    }

    @Test
    void cannotRemoveTheLastItem() {
        Order order = confirmedOrder(List.of(item("Camiseta", 10, "20000")));

        OrderDomainException exception = assertThrows(
                OrderDomainException.class,
                () -> order.removeItem(order.getItems().getFirst().getId())
        );

        assertEquals("An order must have at least one product", exception.getMessage());
        assertEquals(1, order.getItems().size());
    }

    @Test
    void discountRulesAndPaymentFloor() {
        Order order = confirmedOrder(List.of(item("Camiseta", 10, "20000")));

        order.applyDiscount(Money.of(new BigDecimal("20000")));
        assertEquals(Money.of(new BigDecimal("180000")), order.getTotal());

        assertThrows(
                OrderDomainException.class,
                () -> order.applyDiscount(Money.of(new BigDecimal("300000")))
        );
        assertEquals(Money.of(new BigDecimal("20000")), order.getDiscount());
        assertEquals(Money.of(new BigDecimal("180000")), order.getTotal());
        assertThrows(
                IllegalArgumentException.class,
                () -> Money.of(new BigDecimal("-1"))
        );

        order.ensureTotalCoversAmountPaid(Money.of(new BigDecimal("180000")));
        OrderDomainException belowPaid = assertThrows(
                OrderDomainException.class,
                () -> order.ensureTotalCoversAmountPaid(Money.of(new BigDecimal("181000")))
        );
        assertTrue(belowPaid.getMessage().contains("already paid"));
    }

    @Test
    void legacyNullQuotationItemIdRemainsEditable() {
        OrderItem legacy = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Camiseta histórica",
                20,
                "Hydrotech",
                "Azul",
                Money.of(new BigDecimal("45000")),
                ProductSpecification.empty(),
                List.of()
        );
        Order order = orderWithStatus(OrderStatus.IN_PRODUCTION, legacy);

        order.updateItemCommercialCommitment(
                legacy.getId(),
                22,
                Money.of(new BigDecimal("45000"))
        );

        assertNull(order.getItems().getFirst().getQuotationItemId());
        assertEquals(22, order.getItems().getFirst().getQuantity());
        assertEquals(Money.of(new BigDecimal("990000")), order.getTotal());
    }

    private static Order confirmedOrder(List<OrderItem> items) {
        return orderWithStatus(OrderStatus.CONFIRMED, items.toArray(OrderItem[]::new));
    }

    private static Order orderWithStatus(OrderStatus status, OrderItem... items) {
        LocalDate today = LocalDate.of(2026, 8, 23);
        Order created = Order.create(
                OrderNumber.of("ORD-EDIT-" + UUID.randomUUID().toString().substring(0, 8)),
                UUID.randomUUID(),
                UUID.randomUUID(),
                today,
                DeliveryCommitment.of(today.plusDays(7)),
                UUID.randomUUID(),
                null,
                List.of(items)
        );
        if (status == OrderStatus.CONFIRMED) {
            return created;
        }
        return Order.reconstitute(
                created.getId(),
                created.getOrderNumber(),
                created.getCustomerId(),
                created.getQuotationId(),
                created.getConfirmationDate(),
                status,
                created.getDeliveryCommitment(),
                created.getPaymentSummary(),
                created.getSellerId(),
                created.getObservations(),
                created.getDescription(),
                created.getItems(),
                created.getDiscount()
        );
    }

    private static OrderItem item(String name, int quantity, String unitPrice) {
        return OrderItem.reconstitute(
                UUID.randomUUID(),
                name,
                quantity,
                "Sudáfrica",
                "Blanco",
                Money.of(new BigDecimal(unitPrice)),
                ProductSpecification.empty(),
                List.of()
        );
    }
}
