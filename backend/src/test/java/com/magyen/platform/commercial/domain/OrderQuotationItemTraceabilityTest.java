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

/**
 * Trazabilidad comercial QuotationItem → OrderItem.
 * No habilita edición de cotización ni mutación comercial de la orden.
 */
class OrderQuotationItemTraceabilityTest {

    @Test
    void historicalOrderItemsRemainValidWithNullQuotationItemId() {
        OrderItem first = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Camiseta histórica",
                10,
                "Sudáfrica",
                "Blanco",
                Money.of(new BigDecimal("20000")),
                ProductSpecification.empty(),
                List.of()
        );
        OrderItem second = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Pantaloneta histórica",
                4,
                "Microfibra",
                "Negro",
                Money.of(new BigDecimal("15000")),
                ProductSpecification.empty(),
                List.of()
        );

        Order order = confirmedOrder(List.of(first, second));

        assertNull(order.getItems().get(0).getQuotationItemId());
        assertNull(order.getItems().get(1).getQuotationItemId());
        assertEquals(Money.of(new BigDecimal("260000")), order.getTotal());
    }

    @Test
    void rejectsDuplicateQuotationItemReferencesOnTheSameOrder() {
        UUID quotationItemId = UUID.randomUUID();

        OrderItem first = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Camiseta",
                10,
                "Sudáfrica",
                "Blanco",
                Money.of(new BigDecimal("20000")),
                ProductSpecification.empty(),
                List.of(),
                quotationItemId
        );
        OrderItem duplicate = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Camiseta duplicada",
                2,
                "Sudáfrica",
                "Blanco",
                Money.of(new BigDecimal("20000")),
                ProductSpecification.empty(),
                List.of(),
                quotationItemId
        );

        OrderDomainException exception = assertThrows(
                OrderDomainException.class,
                () -> confirmedOrder(List.of(first, duplicate))
        );

        assertEquals(
                "An order cannot reference the same quotation item more than once: " + quotationItemId,
                exception.getMessage()
        );
    }

    @Test
    void tracedOrderItemKeepsSizesSpecificationAndTotal() {
        UUID quotationItemId = UUID.randomUUID();
        ProductSpecification specification = ProductSpecification.of(
                "Camiseta",
                "Redondo",
                "Manga corta sisa",
                true,
                true,
                false,
                false,
                "Full print",
                false,
                false,
                false,
                null,
                null
        );

        OrderItem item = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Camiseta Deportiva",
                20,
                "Hydrotech",
                "Azul",
                Money.of(new BigDecimal("45000")),
                specification,
                List.of(
                        SizeBreakdown.create("S", 3),
                        SizeBreakdown.create("M", 7),
                        SizeBreakdown.create("L", 10)
                ),
                quotationItemId
        );

        Order order = confirmedOrder(List.of(item));

        assertEquals(quotationItemId, order.getItems().getFirst().getQuotationItemId());
        assertEquals(20, order.getItems().getFirst().getQuantity());
        assertEquals(20, order.getItems().getFirst().getAssignedSizeQuantity());
        assertEquals("Camiseta", order.getItems().getFirst().getProductSpecification().getGarmentType());
        assertEquals(Money.of(new BigDecimal("900000")), order.getTotal());
    }

    private static Order confirmedOrder(List<OrderItem> items) {
        LocalDate today = LocalDate.of(2026, 8, 23);
        return Order.create(
                OrderNumber.of("ORD-TRACE-" + UUID.randomUUID().toString().substring(0, 8)),
                UUID.randomUUID(),
                UUID.randomUUID(),
                today,
                DeliveryCommitment.of(today.plusDays(7)),
                UUID.randomUUID(),
                null,
                items
        );
    }
}
