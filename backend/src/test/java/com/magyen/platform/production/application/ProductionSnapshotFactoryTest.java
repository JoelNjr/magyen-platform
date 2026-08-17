package com.magyen.platform.production.application;

import com.magyen.platform.commercial.application.dto.DeliveryCommitmentResult;
import com.magyen.platform.commercial.application.dto.GetOrderResult;
import com.magyen.platform.commercial.application.dto.OrderItemResult;
import com.magyen.platform.commercial.application.dto.PaymentSummaryResult;
import com.magyen.platform.commercial.application.dto.ProductSpecificationResult;
import com.magyen.platform.commercial.application.dto.SizeBreakdownResult;
import com.magyen.platform.commercial.domain.OrderStatus;
import com.magyen.platform.production.domain.ProductSpecification;
import com.magyen.platform.production.domain.ProductionItem;
import com.magyen.platform.production.domain.SizeBreakdown;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductionSnapshotFactoryTest {

    private final ProductionSnapshotFactory snapshotFactory = new ProductionSnapshotFactory();

    @Test
    void captureFromPreservesCommercialProductDataAsIndependentProductionSnapshot() {
        UUID commercialItemId = UUID.randomUUID();
        GetOrderResult order = orderResult(
                OrderStatus.CONFIRMED,
                List.of(new OrderItemResult(
                        commercialItemId,
                        "Camiseta Deportiva",
                        20,
                        "Hydrotech",
                        "Azul",
                        new BigDecimal("45000"),
                        new BigDecimal("900000"),
                        new ProductSpecificationResult(
                                "Camiseta",
                                "Redondo",
                                "Corta",
                                true,
                                true,
                                false,
                                true,
                                "Full print",
                                true,
                                true,
                                false,
                                "Roster completo",
                                "Prioridad alta"
                        ),
                        List.of(
                                new SizeBreakdownResult("S", 3),
                                new SizeBreakdownResult("M", 7),
                                new SizeBreakdownResult("L", 10)
                        )
                ))
        );

        List<ProductionItem> snapshotItems = snapshotFactory.captureFrom(order);

        assertEquals(1, snapshotItems.size());
        ProductionItem productionItem = snapshotItems.getFirst();
        assertEquals("Camiseta Deportiva", productionItem.getProductName());
        assertEquals(20, productionItem.getQuantity());
        assertNotEquals(commercialItemId, productionItem.getId());

        ProductSpecification specification = productionItem.getProductSpecification();
        assertEquals("Camiseta", specification.getGarmentType());
        assertEquals("Redondo", specification.getCollarType());
        assertEquals("Corta", specification.getSleeveType());
        assertEquals(Boolean.TRUE, specification.getCuffRequired());
        assertTrue(specification.isSublimationRequired());
        assertFalse(specification.isEmbroideryRequired());
        assertTrue(specification.isDtfRequired());
        assertEquals("Full print", specification.getDecorationNotes());
        assertTrue(specification.isIncludesNames());
        assertTrue(specification.isIncludesNumbers());
        assertFalse(specification.isIncludesLogos());
        assertEquals("Roster completo", specification.getPersonalizationNotes());
        assertEquals("Prioridad alta", specification.getItemObservations());

        List<SizeBreakdown> sizes = productionItem.getSizeBreakdowns();
        assertEquals(3, sizes.size());
        assertEquals("S", sizes.get(0).getSize());
        assertEquals(3, sizes.get(0).getQuantity());
        assertEquals("M", sizes.get(1).getSize());
        assertEquals(7, sizes.get(1).getQuantity());
        assertEquals("L", sizes.get(2).getSize());
        assertEquals(10, sizes.get(2).getQuantity());
    }

    @Test
    void captureFromAllowsEmptySizes() {
        GetOrderResult order = orderResult(
                OrderStatus.CONFIRMED,
                List.of(new OrderItemResult(
                        UUID.randomUUID(),
                        "Pantalón",
                        12,
                        "Microfibra",
                        "Negro",
                        new BigDecimal("30000"),
                        new BigDecimal("360000"),
                        new ProductSpecificationResult(
                                null, null, null, null,
                                false, false, false, null,
                                false, false, false, null, null
                        ),
                        List.of()
                ))
        );

        List<ProductionItem> snapshotItems = snapshotFactory.captureFrom(order);

        assertEquals(1, snapshotItems.size());
        assertTrue(snapshotItems.getFirst().getSizeBreakdowns().isEmpty());
        assertTrue(snapshotItems.getFirst().getProductSpecification().isEmpty());
    }

    private static GetOrderResult orderResult(OrderStatus status, List<OrderItemResult> items) {
        LocalDate today = LocalDate.now();
        return new GetOrderResult(
                UUID.randomUUID(),
                "ORD-SNAP-TEST",
                "Snapshot description",
                UUID.randomUUID(),
                "Snapshot Customer",
                UUID.randomUUID(),
                1L,
                "C000001",
                today,
                status,
                new DeliveryCommitmentResult(today.plusDays(10), null),
                new PaymentSummaryResult(true, false, new BigDecimal("900000"), new BigDecimal("900000")),
                UUID.randomUUID(),
                "Snapshot Tester",
                "Commercial source",
                items,
                new BigDecimal("900000")
        );
    }
}
