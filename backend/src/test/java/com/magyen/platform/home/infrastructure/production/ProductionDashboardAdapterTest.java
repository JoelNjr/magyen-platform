package com.magyen.platform.home.infrastructure.production;

import com.magyen.platform.home.application.port.ProductionDashboardPort;
import com.magyen.platform.production.application.dto.GetProductionOrdersResult;
import com.magyen.platform.production.application.dto.ProductionOrderResult;
import com.magyen.platform.production.application.usecase.GetProductionOrdersUseCase;
import com.magyen.platform.production.domain.ProductionPriority;
import com.magyen.platform.production.domain.ProductionStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductionDashboardAdapterTest {

    @Mock
    private GetProductionOrdersUseCase getProductionOrdersUseCase;

    @Test
    void emptyProductionOrdersReturnZeroCountsAndEmptyItems() {
        when(getProductionOrdersUseCase.execute()).thenReturn(new GetProductionOrdersResult(List.of()));

        ProductionDashboardPort.HomeProductionSummarySnapshot snapshot =
                new ProductionDashboardAdapter(getProductionOrdersUseCase).getCurrentProductionSummary();

        assertEquals(0, snapshot.totalOrders());
        assertEquals(0, snapshot.createdCount());
        assertEquals(0, snapshot.plannedCount());
        assertEquals(0, snapshot.inProgressCount());
        assertEquals(0, snapshot.completedCount());
        assertTrue(snapshot.items().isEmpty());
    }

    @Test
    void countsAllStatusesAndListsOnlyActiveOrderedDeterministically() {
        UUID createdId = UUID.randomUUID();
        UUID plannedId = UUID.randomUUID();
        UUID inProgressId = UUID.randomUUID();
        UUID completedId = UUID.randomUUID();

        when(getProductionOrdersUseCase.execute()).thenReturn(new GetProductionOrdersResult(List.of(
                order(createdId, ProductionStatus.CREATED, ProductionPriority.NORMAL, LocalDate.of(2026, 8, 1)),
                order(plannedId, ProductionStatus.PLANNED, ProductionPriority.HIGH, LocalDate.of(2026, 8, 2)),
                order(inProgressId, ProductionStatus.IN_PROGRESS, ProductionPriority.LOW, LocalDate.of(2026, 8, 3)),
                order(completedId, ProductionStatus.COMPLETED, ProductionPriority.URGENT, LocalDate.of(2026, 8, 4))
        )));

        ProductionDashboardPort.HomeProductionSummarySnapshot snapshot =
                new ProductionDashboardAdapter(getProductionOrdersUseCase).getCurrentProductionSummary();

        assertEquals(4, snapshot.totalOrders());
        assertEquals(1, snapshot.createdCount());
        assertEquals(1, snapshot.plannedCount());
        assertEquals(1, snapshot.inProgressCount());
        assertEquals(1, snapshot.completedCount());
        assertEquals(3, snapshot.items().size());
        assertEquals(inProgressId, snapshot.items().get(0).productionOrderId());
        assertEquals(plannedId, snapshot.items().get(1).productionOrderId());
        assertEquals(createdId, snapshot.items().get(2).productionOrderId());
        assertTrue(snapshot.items().stream()
                .noneMatch(item -> item.productionOrderId().equals(completedId)));
    }

    @Test
    void passesThroughCommercialOrderNumberAndCustomerName() {
        UUID productionOrderId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        when(getProductionOrdersUseCase.execute()).thenReturn(new GetProductionOrdersResult(List.of(
                new ProductionOrderResult(
                        productionOrderId,
                        orderId,
                        "PED-42",
                        "Camisetas de voleibol",
                        customerId,
                        "Colegio XYZ",
                        LocalDate.of(2026, 8, 9),
                        ProductionStatus.IN_PROGRESS,
                        ProductionPriority.NORMAL,
                        null,
                        null,
                        null,
                        null,
                        null
                )
        )));

        ProductionDashboardPort.HomeProductionSummarySnapshot snapshot =
                new ProductionDashboardAdapter(getProductionOrdersUseCase).getCurrentProductionSummary();

        assertEquals(1, snapshot.items().size());
        assertEquals(productionOrderId, snapshot.items().getFirst().productionOrderId());
        assertEquals(orderId, snapshot.items().getFirst().orderId());
        assertEquals("PED-42", snapshot.items().getFirst().orderNumber());
        assertEquals(customerId, snapshot.items().getFirst().customerId());
        assertEquals("Colegio XYZ", snapshot.items().getFirst().customerName());
        assertEquals("IN_PROGRESS", snapshot.items().getFirst().status());
        assertEquals("NORMAL", snapshot.items().getFirst().priority());
        assertEquals(LocalDate.of(2026, 8, 9), snapshot.items().getFirst().creationDate());
    }

    private static ProductionOrderResult order(
            UUID productionOrderId,
            ProductionStatus status,
            ProductionPriority priority,
            LocalDate creationDate
    ) {
        return new ProductionOrderResult(
                productionOrderId,
                UUID.randomUUID(),
                null,
                null,
                null,
                null,
                creationDate,
                status,
                priority,
                null,
                null,
                null,
                null,
                null
        );
    }
}
