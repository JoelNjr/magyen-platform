package com.magyen.platform.commercial.domain;

import com.magyen.platform.commercial.domain.exception.OrderDomainException;
import com.magyen.platform.shared.domain.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderItemProductSpecificationTest {

    @Test
    void productSpecificationOfKeepsConfiguredCommercialValues() {
        ProductSpecification specification = ProductSpecification.of(
                "Camiseta",
                "Redondo",
                "Manga corta sisa",
                true,
                true,
                false,
                true,
                "Full print",
                true,
                true,
                false,
                "Nombres del roster",
                "Entrega urgente"
        );

        assertEquals("Camiseta", specification.getGarmentType());
        assertEquals("Redondo", specification.getCollarType());
        assertEquals("Manga corta sisa", specification.getSleeveType());
        assertEquals(Boolean.TRUE, specification.getCuffRequired());
        assertTrue(specification.isSublimationRequired());
        assertFalse(specification.isEmbroideryRequired());
        assertTrue(specification.isDtfRequired());
        assertEquals("Full print", specification.getDecorationNotes());
        assertTrue(specification.isIncludesNames());
        assertTrue(specification.isIncludesNumbers());
        assertFalse(specification.isIncludesLogos());
        assertEquals("Nombres del roster", specification.getPersonalizationNotes());
        assertEquals("Entrega urgente", specification.getItemObservations());
    }

    @Test
    void orderItemKeepsCoreCommercialFieldsWithEmptySpecificationAndNoSizes() {
        OrderItem item = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Camiseta Deportiva",
                20,
                "Hydrotech",
                "Azul",
                Money.of(new BigDecimal("45000")),
                ProductSpecification.empty(),
                List.of()
        );

        assertEquals("Camiseta Deportiva", item.getProductName());
        assertEquals(20, item.getQuantity());
        assertEquals("Hydrotech", item.getFabric());
        assertEquals("Azul", item.getColor());
        assertEquals(Money.of(new BigDecimal("45000")), item.getUnitPrice());
        assertEquals(Money.of(new BigDecimal("900000")), item.getSubtotal());
        assertTrue(item.getProductSpecification().isEmpty());
        assertTrue(item.getSizeBreakdowns().isEmpty());
    }

    @Test
    void sizeBreakdownAcceptsExactMatchForItemQuantity() {
        OrderItem item = orderItemWithQuantity(20);

        item.replaceSizeBreakdowns(List.of(
                SizeBreakdown.create("S", 3),
                SizeBreakdown.create("M", 7),
                SizeBreakdown.create("L", 6),
                SizeBreakdown.create("XL", 4)
        ));

        assertEquals(4, item.getSizeBreakdowns().size());
        assertEquals(20, item.getAssignedSizeQuantity());
        assertEquals(20, item.getQuantity());
    }

    @Test
    void sizeBreakdownRejectsBlankSize() {
        assertThrows(OrderDomainException.class, () -> SizeBreakdown.create("   ", 3));
    }

    @Test
    void sizeBreakdownRejectsZeroQuantity() {
        assertThrows(OrderDomainException.class, () -> SizeBreakdown.create("S", 0));
    }

    @Test
    void sizeBreakdownRejectsNegativeQuantity() {
        assertThrows(OrderDomainException.class, () -> SizeBreakdown.create("S", -1));
    }

    @Test
    void sizeBreakdownRejectsDuplicateSize() {
        OrderItem item = orderItemWithQuantity(20);

        assertThrows(
                OrderDomainException.class,
                () -> item.replaceSizeBreakdowns(List.of(
                        SizeBreakdown.create("M", 7),
                        SizeBreakdown.create("M", 3)
                ))
        );
    }

    @Test
    void sizeBreakdownRejectsTotalGreaterThanItemQuantity() {
        OrderItem item = orderItemWithQuantity(20);

        assertThrows(
                OrderDomainException.class,
                () -> item.replaceSizeBreakdowns(List.of(
                        SizeBreakdown.create("S", 10),
                        SizeBreakdown.create("M", 11)
                ))
        );
    }

    @Test
    void existingOrderItemWithoutSizesRemainsValid() {
        OrderItem item = orderItemWithQuantity(20);

        assertTrue(item.getSizeBreakdowns().isEmpty());
        assertEquals(0, item.getAssignedSizeQuantity());
        assertEquals(20, item.getQuantity());
    }

    private OrderItem orderItemWithQuantity(int quantity) {
        return OrderItem.reconstitute(
                UUID.randomUUID(),
                "Camiseta Deportiva",
                quantity,
                "Hydrotech",
                "Azul",
                Money.of(new BigDecimal("45000")),
                ProductSpecification.empty(),
                List.of()
        );
    }
}
