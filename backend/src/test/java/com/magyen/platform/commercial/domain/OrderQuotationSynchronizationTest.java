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

class OrderQuotationSynchronizationTest {

    @Test
    void applyUpdatesMatchedAddsNewRemovesOrphansAndLeavesManualUntouched() {
        UUID matchedQuotationItemId = UUID.randomUUID();
        UUID orphanQuotationItemId = UUID.randomUUID();
        UUID newQuotationItemId = UUID.randomUUID();

        OrderItem matched = tracedItem(
                matchedQuotationItemId,
                "Camiseta",
                20,
                "Sudáfrica",
                "Blanco",
                "40000"
        );
        UUID matchedOrderItemId = matched.getId();
        OrderItem orphan = tracedItem(orphanQuotationItemId, "Buzo", 5, "Hydrotech", "Negro", "30000");
        OrderItem manual = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Extra manual",
                3,
                "Perchada",
                "Rojo",
                Money.of(new BigDecimal("10000")),
                ProductSpecification.empty(),
                List.of()
        );

        Order order = confirmedOrder(List.of(matched, orphan, manual));
        String orderNumber = order.getOrderNumber().getValue();

        QuotationItem updated = QuotationItem.reconstitute(
                matchedQuotationItemId,
                "Camiseta polo",
                18,
                "Hydrotech",
                "Perchada",
                "Blanco",
                Money.of(new BigDecimal("42000")),
                ProductSpecification.of(
                        "Camiseta",
                        "Redondo",
                        "Manga corta sisa",
                        false,
                        true,
                        false,
                        false,
                        null,
                        false,
                        false,
                        false,
                        null,
                        "Actualizada"
                )
        );
        QuotationItem added = QuotationItem.reconstitute(
                newQuotationItemId,
                "Pantalón",
                4,
                "Sudáfrica",
                null,
                "Negro",
                Money.of(new BigDecimal("25000")),
                ProductSpecification.empty()
        );

        order.applyQuotationCommercialSource(List.of(updated, added), Money.of(new BigDecimal("10000")));

        assertEquals(orderNumber, order.getOrderNumber().getValue());
        assertEquals(3, order.getItems().size());
        OrderItem synced = order.getItems().stream()
                .filter(item -> matchedQuotationItemId.equals(item.getQuotationItemId()))
                .findFirst()
                .orElseThrow();
        assertEquals(matchedOrderItemId, synced.getId());
        assertEquals("Camiseta polo", synced.getProductName());
        assertEquals("Hydrotech", synced.getFabric());
        assertEquals("Perchada", synced.getSecondaryFabric());
        assertEquals(18, synced.getQuantity());
        assertEquals(Money.of(new BigDecimal("42000")), synced.getUnitPrice());
        assertEquals("Actualizada", synced.getProductSpecification().getItemObservations());
        assertTrue(synced.getSizeBreakdowns().isEmpty());

        OrderItem created = order.getItems().stream()
                .filter(item -> newQuotationItemId.equals(item.getQuotationItemId()))
                .findFirst()
                .orElseThrow();
        assertTrue(created.getSizeBreakdowns().isEmpty());
        assertEquals("Pantalón", created.getProductName());

        assertTrue(order.getItems().stream().noneMatch(item -> orphanQuotationItemId.equals(item.getQuotationItemId())));
        OrderItem untouchedManual = order.getItems().stream()
                .filter(item -> item.getQuotationItemId() == null)
                .findFirst()
                .orElseThrow();
        assertEquals("Extra manual", untouchedManual.getProductName());
        assertEquals(3, untouchedManual.getQuantity());
        assertEquals(Money.of(new BigDecimal("10000")), order.getDiscount());
    }

    @Test
    void applyIsRejectedForLegacyUntracedOrders() {
        Order order = confirmedOrder(List.of(OrderItem.reconstitute(
                UUID.randomUUID(),
                "Histórica",
                10,
                "Sudáfrica",
                "Blanco",
                Money.of(new BigDecimal("20000")),
                ProductSpecification.empty(),
                List.of()
        )));
        QuotationItem quotationItem = QuotationItem.reconstitute(
                UUID.randomUUID(),
                "Camiseta",
                8,
                "Sudáfrica",
                "Blanco",
                Money.of(new BigDecimal("20000")),
                ProductSpecification.empty()
        );

        OrderDomainException exception = assertThrows(
                OrderDomainException.class,
                () -> order.applyQuotationCommercialSource(List.of(quotationItem), Money.zero())
        );
        assertTrue(exception.getMessage().contains("traceability"));
        assertEquals(10, order.getItems().getFirst().getQuantity());
        assertNull(order.getItems().getFirst().getQuotationItemId());
    }

    @Test
    void applyIsRejectedWhenOrderIsFrozen() {
        UUID quotationItemId = UUID.randomUUID();
        for (OrderStatus status : List.of(OrderStatus.DELIVERED, OrderStatus.CLOSED)) {
            Order order = orderWithStatus(status, tracedItem(quotationItemId, "Camiseta", 10, "Sudáfrica", "Blanco", "20000"));
            QuotationItem quotationItem = QuotationItem.reconstitute(
                    quotationItemId,
                    "Camiseta",
                    12,
                    "Sudáfrica",
                    "Blanco",
                    Money.of(new BigDecimal("20000")),
                    ProductSpecification.empty()
            );
            OrderDomainException exception = assertThrows(
                    OrderDomainException.class,
                    () -> order.applyQuotationCommercialSource(List.of(quotationItem), Money.zero())
            );
            assertTrue(exception.getMessage().contains("CONFIRMED, IN_PRODUCTION or READY_FOR_DELIVERY"));
            assertEquals(10, order.getItems().getFirst().getQuantity());
        }
    }

    @Test
    void applyWorksForInProductionAndReadyForDelivery() {
        UUID quotationItemId = UUID.randomUUID();
        for (OrderStatus status : List.of(OrderStatus.IN_PRODUCTION, OrderStatus.READY_FOR_DELIVERY)) {
            Order order = orderWithStatus(
                    status,
                    tracedItem(quotationItemId, "Camiseta", 10, "Sudáfrica", "Blanco", "20000")
            );
            order.applyQuotationCommercialSource(
                    List.of(QuotationItem.reconstitute(
                            quotationItemId,
                            "Camiseta",
                            11,
                            "Sudáfrica",
                            "Blanco",
                            Money.of(new BigDecimal("20000")),
                            ProductSpecification.empty()
                    )),
                    Money.zero()
            );
            assertEquals(11, order.getItems().getFirst().getQuantity());
            assertEquals(status, order.getStatus());
        }
    }

    @Test
    void applyIsRejectedWhenNewQuantityIsBelowAssignedSizesAndNothingChanges() {
        UUID quotationItemId = UUID.randomUUID();
        OrderItem traced = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Camiseta",
                20,
                "Sudáfrica",
                "Blanco",
                Money.of(new BigDecimal("40000")),
                ProductSpecification.empty(),
                List.of(SizeBreakdown.create("S", 8), SizeBreakdown.create("M", 8)),
                quotationItemId
        );
        Order order = confirmedOrder(List.of(traced));

        OrderDomainException exception = assertThrows(
                OrderDomainException.class,
                () -> order.applyQuotationCommercialSource(
                        List.of(QuotationItem.reconstitute(
                                quotationItemId,
                                "Camiseta",
                                10,
                                "Sudáfrica",
                                "Blanco",
                                Money.of(new BigDecimal("40000")),
                                ProductSpecification.empty()
                        )),
                        Money.zero()
                )
        );
        assertTrue(exception.getMessage().contains("size quantity"));
        assertEquals(20, order.getItems().getFirst().getQuantity());
        assertEquals(16, order.getItems().getFirst().getAssignedSizeQuantity());
    }

    private static OrderItem tracedItem(
            UUID quotationItemId,
            String name,
            int quantity,
            String fabric,
            String color,
            String unitPrice
    ) {
        return OrderItem.reconstitute(
                UUID.randomUUID(),
                name,
                quantity,
                fabric,
                color,
                Money.of(new BigDecimal(unitPrice)),
                ProductSpecification.empty(),
                List.of(),
                quotationItemId
        );
    }

    private static Order confirmedOrder(List<OrderItem> items) {
        return orderWithStatus(OrderStatus.CONFIRMED, items.toArray(OrderItem[]::new));
    }

    private static Order orderWithStatus(OrderStatus status, OrderItem... items) {
        LocalDate today = LocalDate.of(2026, 8, 20);
        Order created = Order.create(
                OrderNumber.of("ORD-SYNC-" + UUID.randomUUID().toString().substring(0, 8)),
                UUID.randomUUID(),
                UUID.randomUUID(),
                today,
                DeliveryCommitment.of(today.plusDays(10)),
                UUID.randomUUID(),
                "Sincronización",
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
}
