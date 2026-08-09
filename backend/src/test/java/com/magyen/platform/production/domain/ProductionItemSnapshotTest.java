package com.magyen.platform.production.domain;

import com.magyen.platform.production.domain.exception.ProductionDomainException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductionItemSnapshotTest {

    @Test
    void productionItemPreservesProductNameAndQuantity() {
        ProductionItem item = ProductionItem.create(
                "Camiseta Deportiva",
                20,
                ProductSpecification.empty(),
                List.of()
        );

        assertEquals("Camiseta Deportiva", item.getProductName());
        assertEquals(20, item.getQuantity());
        assertTrue(item.getProductSpecification().isEmpty());
        assertTrue(item.getSizeBreakdowns().isEmpty());
    }

    @Test
    void productionItemPreservesProductSpecification() {
        ProductSpecification specification = ProductSpecification.of(
                "Camiseta",
                "Redondo",
                "Corta",
                "Dry-fit",
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

        ProductionItem item = ProductionItem.create(
                "Camiseta Deportiva",
                20,
                specification,
                List.of()
        );

        ProductSpecification preserved = item.getProductSpecification();
        assertEquals("Camiseta", preserved.getGarmentType());
        assertEquals("Redondo", preserved.getCollarType());
        assertEquals("Corta", preserved.getSleeveType());
        assertEquals("Dry-fit", preserved.getGarmentVariant());
        assertTrue(preserved.isSublimationRequired());
        assertFalse(preserved.isEmbroideryRequired());
        assertTrue(preserved.isDtfRequired());
        assertEquals("Full print", preserved.getDecorationNotes());
        assertTrue(preserved.isIncludesNames());
        assertTrue(preserved.isIncludesNumbers());
        assertFalse(preserved.isIncludesLogos());
        assertEquals("Nombres del roster", preserved.getPersonalizationNotes());
        assertEquals("Entrega urgente", preserved.getItemObservations());
    }

    @Test
    void productionItemPreservesSizeBreakdown() {
        ProductionItem item = ProductionItem.create(
                "Camiseta Deportiva",
                20,
                ProductSpecification.empty(),
                List.of(
                        SizeBreakdown.create("S", 3),
                        SizeBreakdown.create("M", 7),
                        SizeBreakdown.create("L", 6),
                        SizeBreakdown.create("XL", 4)
                )
        );

        assertEquals(4, item.getSizeBreakdowns().size());
        assertEquals(20, item.getAssignedSizeQuantity());
        assertEquals("S", item.getSizeBreakdowns().get(0).getSize());
        assertEquals(3, item.getSizeBreakdowns().get(0).getQuantity());
        assertEquals("M", item.getSizeBreakdowns().get(1).getSize());
        assertEquals(7, item.getSizeBreakdowns().get(1).getQuantity());
    }

    @Test
    void emptySizesRemainValid() {
        ProductionItem item = ProductionItem.create(
                "Camiseta Deportiva",
                15,
                ProductSpecification.empty(),
                List.of()
        );

        assertTrue(item.getSizeBreakdowns().isEmpty());
        assertEquals(0, item.getAssignedSizeQuantity());
    }

    @Test
    void productionOrderKeepsSnapshotItemsAndExistingLifecycle() {
        ProductionItem item = ProductionItem.create(
                "Camiseta Deportiva",
                10,
                ProductSpecification.of(
                        "Camiseta",
                        "V",
                        "Larga",
                        null,
                        false,
                        true,
                        false,
                        null,
                        false,
                        false,
                        true,
                        null,
                        "Sin tallas asignadas"
                ),
                List.of()
        );

        ProductionOrder productionOrder = ProductionOrder.create(
                UUID.randomUUID(),
                LocalDate.now(),
                ProductionPriority.NORMAL,
                null,
                null,
                "Snapshot foundation",
                List.of(item)
        );

        assertEquals(ProductionStatus.CREATED, productionOrder.getStatus());
        assertEquals(1, productionOrder.getItems().size());
        assertEquals("Camiseta Deportiva", productionOrder.getItems().getFirst().getProductName());
        assertEquals(10, productionOrder.getItems().getFirst().getQuantity());
        assertTrue(productionOrder.getItems().getFirst().getSizeBreakdowns().isEmpty());

        LocalDate plannedStart = LocalDate.now().plusDays(1);
        LocalDate plannedEnd = LocalDate.now().plusDays(5);
        productionOrder.plan(plannedStart, plannedEnd, ProductionPriority.HIGH);
        assertEquals(ProductionStatus.PLANNED, productionOrder.getStatus());

        productionOrder.start();
        assertEquals(ProductionStatus.IN_PROGRESS, productionOrder.getStatus());

        productionOrder.complete();
        assertEquals(ProductionStatus.COMPLETED, productionOrder.getStatus());
        assertEquals(1, productionOrder.getItems().size());
    }

    @Test
    void sizeBreakdownSumCannotExceedItemQuantity() {
        assertThrows(ProductionDomainException.class, () -> ProductionItem.create(
                "Camiseta Deportiva",
                10,
                ProductSpecification.empty(),
                List.of(
                        SizeBreakdown.create("S", 6),
                        SizeBreakdown.create("M", 5)
                )
        ));
    }
}
