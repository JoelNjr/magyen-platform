package com.magyen.platform.production.application.usecase;

import com.magyen.platform.commercial.application.dto.ProductSpecificationCommand;
import com.magyen.platform.commercial.application.dto.UpdateOrderItemProductSpecificationCommand;
import com.magyen.platform.commercial.application.usecase.UpdateOrderItemProductSpecificationUseCase;
import com.magyen.platform.commercial.domain.DeliveryCommitment;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderItem;
import com.magyen.platform.commercial.domain.OrderNumber;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.ProductSpecification;
import com.magyen.platform.commercial.domain.SizeBreakdown;
import com.magyen.platform.production.application.dto.CreateProductionOrderCommand;
import com.magyen.platform.production.application.dto.CreateProductionOrderResult;
import com.magyen.platform.production.application.dto.GetProductionOrderCommand;
import com.magyen.platform.production.application.dto.GetProductionOrderResult;
import com.magyen.platform.production.application.dto.ProductionItemResult;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import com.magyen.platform.production.domain.ProductionPriority;
import com.magyen.platform.production.domain.ProductionStatus;
import com.magyen.platform.production.presentation.productionorder.mapper.ProductionPresentationMapper;
import com.magyen.platform.production.presentation.productionorder.response.GetProductionOrderResponse;
import com.magyen.platform.production.presentation.productionorder.response.ProductionItemResponse;
import com.magyen.platform.shared.domain.Money;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica que el contrato de lectura de Producción exponga el snapshot persistido.
 */
@SpringBootTest
@Transactional
class GetProductionOrderSnapshotExposureTest {

    @Autowired
    private CreateProductionOrderFromOrderUseCase createProductionOrderFromOrderUseCase;

    @Autowired
    private GetProductionOrderUseCase getProductionOrderUseCase;

    @Autowired
    private UpdateOrderItemProductSpecificationUseCase updateOrderItemProductSpecificationUseCase;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductionOrderRepository productionOrderRepository;

    @Autowired
    private ProductionPresentationMapper productionPresentationMapper;

    @Autowired
    private EntityManager entityManager;

    @Test
    void getProductionOrderExposesPersistedSnapshotItemsSpecificationsAndSizes() {
        Order commercialOrder = persistConfirmedOrderWithSnapshotData();

        CreateProductionOrderResult created = createProductionOrderFromOrderUseCase.execute(
                new CreateProductionOrderCommand(
                        commercialOrder.getId(),
                        ProductionPriority.NORMAL,
                        null,
                        null,
                        "Snapshot exposure"
                )
        );

        entityManager.flush();
        entityManager.clear();

        GetProductionOrderResult result = getProductionOrderUseCase.execute(
                new GetProductionOrderCommand(created.productionOrderId())
        );

        assertEquals(created.productionOrderId(), result.productionOrderId());
        assertEquals(commercialOrder.getId(), result.orderId());
        assertEquals(ProductionStatus.CREATED, result.status());
        assertEquals(1, result.items().size());

        ProductionItemResult item = result.items().getFirst();
        assertEquals("Camiseta Deportiva", item.productName());
        assertEquals(20, item.quantity());
        assertEquals("Camiseta", item.productSpecification().garmentType());
        assertEquals("Redondo", item.productSpecification().collarType());
        assertEquals("Corta", item.productSpecification().sleeveType());
        assertEquals("Dry-fit", item.productSpecification().garmentVariant());
        assertTrue(item.productSpecification().sublimationRequired());
        assertEquals("Full print", item.productSpecification().decorationNotes());
        assertTrue(item.productSpecification().includesNames());
        assertEquals("Roster completo", item.productSpecification().personalizationNotes());
        assertEquals("Prioridad alta", item.productSpecification().itemObservations());
        assertEquals(
                Map.of("S", 3, "M", 7, "L", 10),
                item.sizes().stream().collect(Collectors.toMap(
                        size -> size.size(),
                        size -> size.quantity()
                ))
        );

        GetProductionOrderResponse response = productionPresentationMapper.toResponse(result);
        assertEquals(1, response.items().size());
        ProductionItemResponse responseItem = response.items().getFirst();
        assertEquals(item.productionItemId(), responseItem.productionItemId());
        assertEquals("Camiseta Deportiva", responseItem.productName());
        assertEquals(20, responseItem.quantity());
        assertEquals("Camiseta", responseItem.productSpecification().garmentType());
        assertEquals(3, responseItem.sizes().size());
    }

    @Test
    void getProductionOrderSnapshotRemainsIndependentFromLaterCommercialChanges() {
        Order commercialOrder = persistConfirmedOrderWithSnapshotData();
        UUID commercialItemId = commercialOrder.getItems().getFirst().getId();

        CreateProductionOrderResult created = createProductionOrderFromOrderUseCase.execute(
                new CreateProductionOrderCommand(
                        commercialOrder.getId(),
                        ProductionPriority.NORMAL,
                        null,
                        null,
                        "Independence exposure"
                )
        );

        updateOrderItemProductSpecificationUseCase.execute(
                new UpdateOrderItemProductSpecificationCommand(
                        commercialOrder.getId(),
                        commercialItemId,
                        new ProductSpecificationCommand(
                                "Polo",
                                "Mao",
                                "Larga",
                                "Premium",
                                false,
                                true,
                                false,
                                "Changed after snapshot",
                                false,
                                false,
                                true,
                                "New notes",
                                "Changed"
                        )
                )
        );

        entityManager.flush();
        entityManager.clear();

        GetProductionOrderResult result = getProductionOrderUseCase.execute(
                new GetProductionOrderCommand(created.productionOrderId())
        );

        assertEquals("Camiseta", result.items().getFirst().productSpecification().garmentType());
        assertEquals("Full print", result.items().getFirst().productSpecification().decorationNotes());
        assertEquals(
                Map.of("S", 3, "M", 7, "L", 10),
                result.items().getFirst().sizes().stream().collect(Collectors.toMap(
                        size -> size.size(),
                        size -> size.quantity()
                ))
        );
    }

    @Test
    void getProductionOrderWithEmptyHistoricalSnapshotRemainsValid() {
        ProductionOrder historicalProductionOrder = ProductionOrder.create(
                UUID.randomUUID(),
                LocalDate.now(),
                ProductionPriority.NORMAL,
                null,
                null,
                "Historical empty snapshot",
                List.of()
        );
        ProductionOrder saved = productionOrderRepository.save(historicalProductionOrder);

        entityManager.flush();
        entityManager.clear();

        GetProductionOrderResult result = getProductionOrderUseCase.execute(
                new GetProductionOrderCommand(saved.getId())
        );

        assertEquals(saved.getId(), result.productionOrderId());
        assertTrue(result.items().isEmpty());
        assertTrue(result.operations().isEmpty());

        GetProductionOrderResponse response = productionPresentationMapper.toResponse(result);
        assertTrue(response.items().isEmpty());
    }

    private Order persistConfirmedOrderWithSnapshotData() {
        OrderItem item = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Camiseta Deportiva",
                20,
                "Hydrotech",
                "Azul",
                Money.of(new BigDecimal("45000")),
                ProductSpecification.of(
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
                        "Roster completo",
                        "Prioridad alta"
                ),
                List.of(
                        SizeBreakdown.create("S", 3),
                        SizeBreakdown.create("M", 7),
                        SizeBreakdown.create("L", 10)
                )
        );

        LocalDate today = LocalDate.now();
        Order order = Order.create(
                OrderNumber.of("ORD-EXP-" + UUID.randomUUID().toString().substring(0, 8)),
                UUID.randomUUID(),
                UUID.randomUUID(),
                today,
                DeliveryCommitment.of(today.plusDays(10)),
                "Exposure Tester",
                "Snapshot exposure order",
                List.of(item)
        );

        Order saved = orderRepository.save(order);
        entityManager.flush();
        entityManager.clear();
        return orderRepository.findById(saved.getId()).orElseThrow();
    }
}
