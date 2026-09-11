package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.CommercialOrderIdentityResolver;
import com.magyen.platform.production.application.CommercialOrderIdentityResolver.CommercialOrderIdentity;
import com.magyen.platform.production.application.dto.GetProductionOrdersQuery;
import com.magyen.platform.production.application.dto.GetProductionOrdersResult;
import com.magyen.platform.production.application.dto.ProductionOrderResult;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import com.magyen.platform.production.domain.ProductionPriority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashMap;
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

    private GetProductionOrdersUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetProductionOrdersUseCase(productionOrderRepository, commercialOrderIdentityResolver);
    }

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
                new CommercialOrderIdentity(orderId, "PED-42", "Uniformes colegio", customerId, "Colegio XYZ")
        ));

        GetProductionOrdersResult result = useCase.execute();

        ProductionOrderResult item = result.productionOrders().getFirst();
        assertEquals(productionOrder.getId(), item.productionOrderId());
        assertEquals(orderId, item.orderId());
        assertEquals("PED-42", item.orderNumber());
        assertEquals("Uniformes colegio", item.orderDescription());
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

        ProductionOrderResult item = useCase.execute().productionOrders().getFirst();

        assertEquals(productionOrder.getId(), item.productionOrderId());
        assertEquals(orderId, item.orderId());
        assertNull(item.orderNumber());
        assertNull(item.customerName());
    }

    @Test
    void listsProductionOrdersByNumericCommercialOrderNumber() {
        Map<UUID, CommercialOrderIdentity> identities = new HashMap<>();
        List<ProductionOrder> productionOrders = List.of(
                productionOrder("12", LocalDate.of(2026, 5, 1), identities),
                productionOrder("1", LocalDate.of(2026, 5, 20), identities),
                productionOrder("10", LocalDate.of(2026, 5, 3), identities),
                productionOrder("2", LocalDate.of(2026, 5, 2), identities),
                productionOrder("11", LocalDate.of(2026, 5, 4), identities),
                productionOrder("9", LocalDate.of(2026, 5, 5), identities)
        );
        when(productionOrderRepository.findAll()).thenReturn(productionOrders);
        when(commercialOrderIdentityResolver.resolveAll()).thenReturn(identities);

        List<String> numbers = useCase.execute().productionOrders().stream()
                .map(ProductionOrderResult::orderNumber)
                .toList();

        assertEquals(List.of("1", "2", "9", "10", "11", "12"), numbers);
    }

    @Test
    void updatedProductionOrderDoesNotJumpToTheFront() {
        Map<UUID, CommercialOrderIdentity> identities = new HashMap<>();
        ProductionOrder first = productionOrder("13", LocalDate.of(2026, 5, 2), identities);
        ProductionOrder second = productionOrder("14", LocalDate.of(2026, 5, 3), identities);
        ProductionOrder third = productionOrder("15", LocalDate.of(2026, 5, 1), identities);
        when(productionOrderRepository.findAll()).thenReturn(List.of(third, first, second));
        when(commercialOrderIdentityResolver.resolveAll()).thenReturn(identities);

        List<String> numbers = useCase.execute().productionOrders().stream()
                .map(ProductionOrderResult::orderNumber)
                .toList();

        assertEquals(List.of("13", "14", "15"), numbers);
    }

    @Test
    void creationDateFilterStillAppliesAndKeepsConsecutiveOrder() {
        Map<UUID, CommercialOrderIdentity> identities = new HashMap<>();
        ProductionOrder inRangeHigh = productionOrder("20", LocalDate.of(2026, 6, 10), identities);
        ProductionOrder inRangeLow = productionOrder("9", LocalDate.of(2026, 6, 20), identities);
        ProductionOrder outOfRange = productionOrder("11", LocalDate.of(2026, 7, 1), identities);
        when(productionOrderRepository.findAll()).thenReturn(List.of(inRangeHigh, outOfRange, inRangeLow));
        when(commercialOrderIdentityResolver.resolveAll()).thenReturn(identities);

        List<String> numbers = useCase.execute(
                new GetProductionOrdersQuery(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30))
        ).productionOrders().stream().map(ProductionOrderResult::orderNumber).toList();

        assertEquals(List.of("9", "20"), numbers);
    }

    private static ProductionOrder productionOrder(
            String commercialNumber,
            LocalDate creationDate,
            Map<UUID, CommercialOrderIdentity> identities
    ) {
        UUID orderId = UUID.randomUUID();
        identities.put(
                orderId,
                new CommercialOrderIdentity(orderId, commercialNumber, "Pedido " + commercialNumber, UUID.randomUUID(), "Cliente")
        );
        return ProductionOrder.create(
                orderId,
                creationDate,
                ProductionPriority.NORMAL,
                null,
                null,
                "PO " + commercialNumber
        );
    }
}
