package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.CommercialOrderIdentityResolver;
import com.magyen.platform.production.application.CommercialOrderIdentityResolver.CommercialOrderIdentity;
import com.magyen.platform.production.application.dto.GetProductionOrdersResult;
import com.magyen.platform.production.application.dto.ProductionOrderResult;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import com.magyen.platform.production.domain.ProductionPriority;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProductionOrdersUseCaseTest {

    @Mock
    private ProductionOrderRepository productionOrderRepository;

    @Mock
    private CommercialOrderIdentityResolver commercialOrderIdentityResolver;

    @Test
    void enrichesListWithCommercialOrderNumberAndCustomerName() {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        ProductionOrder productionOrder = ProductionOrder.create(
                orderId,
                LocalDate.of(2026, 8, 9),
                ProductionPriority.NORMAL,
                null,
                null,
                "Readable identity"
        );

        when(productionOrderRepository.findAll()).thenReturn(List.of(productionOrder));
        when(commercialOrderIdentityResolver.resolveAll()).thenReturn(Map.of(
                orderId,
                new CommercialOrderIdentity(orderId, "PED-42", customerId, "Colegio XYZ")
        ));

        GetProductionOrdersResult result = new GetProductionOrdersUseCase(
                productionOrderRepository,
                commercialOrderIdentityResolver
        ).execute();

        ProductionOrderResult item = result.productionOrders().getFirst();
        assertEquals(productionOrder.getId(), item.productionOrderId());
        assertEquals(orderId, item.orderId());
        assertEquals("PED-42", item.orderNumber());
        assertEquals(customerId, item.customerId());
        assertEquals("Colegio XYZ", item.customerName());
        assertEquals(LocalDate.of(2026, 8, 9), item.creationDate());
        assertEquals(ProductionPriority.NORMAL, item.priority());
    }

    @Test
    void keepsInternalIdsWhenCommercialIdentityIsMissing() {
        UUID orderId = UUID.randomUUID();
        ProductionOrder productionOrder = ProductionOrder.create(
                orderId,
                LocalDate.of(2026, 8, 9),
                ProductionPriority.NORMAL,
                null,
                null,
                null
        );
        when(productionOrderRepository.findAll()).thenReturn(List.of(productionOrder));
        when(commercialOrderIdentityResolver.resolveAll()).thenReturn(Map.of());

        ProductionOrderResult item = new GetProductionOrdersUseCase(
                productionOrderRepository,
                commercialOrderIdentityResolver
        ).execute().productionOrders().getFirst();

        assertEquals(productionOrder.getId(), item.productionOrderId());
        assertEquals(orderId, item.orderId());
        assertNull(item.orderNumber());
        assertNull(item.customerName());
    }
}
