package com.magyen.platform.production.application.usecase;

import com.magyen.platform.commercial.application.dto.ProductSpecificationCommand;
import com.magyen.platform.commercial.application.dto.ReplaceOrderItemSizesCommand;
import com.magyen.platform.commercial.application.dto.SizeBreakdownCommand;
import com.magyen.platform.commercial.application.dto.UpdateOrderItemProductSpecificationCommand;
import com.magyen.platform.commercial.application.usecase.ReplaceOrderItemSizesUseCase;
import com.magyen.platform.commercial.application.usecase.UpdateOrderItemProductSpecificationUseCase;
import com.magyen.platform.commercial.domain.DeliveryCommitment;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderItem;
import com.magyen.platform.commercial.domain.OrderNumber;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.ProductSpecification;
import com.magyen.platform.commercial.domain.SizeBreakdown;
import com.magyen.platform.production.application.dto.AddProductionOperationCommand;
import com.magyen.platform.production.application.dto.CompleteProductionOperationCommand;
import com.magyen.platform.production.application.dto.CompleteProductionOrderCommand;
import com.magyen.platform.production.application.dto.CreateProductionOrderCommand;
import com.magyen.platform.production.application.dto.CreateProductionOrderResult;
import com.magyen.platform.production.application.dto.PlanProductionOrderCommand;
import com.magyen.platform.production.application.dto.StartProductionOperationCommand;
import com.magyen.platform.production.application.dto.StartProductionOrderCommand;
import com.magyen.platform.production.domain.ProductionItem;
import com.magyen.platform.production.domain.ProductionOperationType;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import com.magyen.platform.production.domain.ProductionPriority;
import com.magyen.platform.production.domain.ProductionStatus;
import com.magyen.platform.production.domain.exception.ProductionDomainException;
import com.magyen.platform.production.domain.exception.ProductionOrderAlreadyExistsException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica la creación de Orden de Producción con snapshot desde una Orden comercial válida.
 */
@SpringBootTest
@Transactional
class CreateProductionOrderFromOrderUseCaseTest {

    @Autowired
    private CreateProductionOrderFromOrderUseCase createProductionOrderFromOrderUseCase;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductionOrderRepository productionOrderRepository;

    @Autowired
    private UpdateOrderItemProductSpecificationUseCase updateOrderItemProductSpecificationUseCase;

    @Autowired
    private ReplaceOrderItemSizesUseCase replaceOrderItemSizesUseCase;

    @Autowired
    private AddProductionOperationUseCase addProductionOperationUseCase;

    @Autowired
    private PlanProductionOrderUseCase planProductionOrderUseCase;

    @Autowired
    private StartProductionOrderUseCase startProductionOrderUseCase;

    @Autowired
    private StartProductionOperationUseCase startProductionOperationUseCase;

    @Autowired
    private CompleteProductionOperationUseCase completeProductionOperationUseCase;

    @Autowired
    private CompleteProductionOrderUseCase completeProductionOrderUseCase;

    @Autowired
    private EntityManager entityManager;

    @Test
    void createsProductionOrderWithFullSnapshotFromConfirmedCommercialOrder() {
        Order commercialOrder = persistConfirmedOrderWithTwoItems();

        CreateProductionOrderResult result = createProductionOrderFromOrderUseCase.execute(
                new CreateProductionOrderCommand(
                        commercialOrder.getId(),
                        ProductionPriority.HIGH,
                        LocalDate.now().plusDays(1),
                        LocalDate.now().plusDays(10),
                        "From commercial order"
                )
        );

        ProductionOrder productionOrder = reloadProductionOrder(result.productionOrderId());

        assertEquals(result.productionOrderId(), productionOrder.getId());
        assertEquals(commercialOrder.getId(), productionOrder.getOrderId());
        assertEquals(ProductionStatus.CREATED, productionOrder.getStatus());
        assertEquals(ProductionPriority.HIGH, productionOrder.getPriority());
        assertEquals(2, productionOrder.getItems().size());

        ProductionItem firstItem = findItemByProductName(productionOrder, "Camiseta Deportiva");
        assertEquals(20, firstItem.getQuantity());
        assertEquals("Camiseta", firstItem.getProductSpecification().getGarmentType());
        assertEquals("Redondo", firstItem.getProductSpecification().getCollarType());
        assertEquals("Corta", firstItem.getProductSpecification().getSleeveType());
        assertEquals("Dry-fit", firstItem.getProductSpecification().getGarmentVariant());
        assertTrue(firstItem.getProductSpecification().isSublimationRequired());
        assertEquals("Full print", firstItem.getProductSpecification().getDecorationNotes());
        assertTrue(firstItem.getProductSpecification().isIncludesNames());
        assertEquals("Roster completo", firstItem.getProductSpecification().getPersonalizationNotes());
        assertEquals("Prioridad alta", firstItem.getProductSpecification().getItemObservations());

        Map<String, Integer> firstSizes = firstItem.getSizeBreakdowns().stream()
                .collect(Collectors.toMap(
                        com.magyen.platform.production.domain.SizeBreakdown::getSize,
                        com.magyen.platform.production.domain.SizeBreakdown::getQuantity
                ));
        assertEquals(Map.of("S", 3, "M", 7, "L", 10), firstSizes);

        ProductionItem secondItem = findItemByProductName(productionOrder, "Pantalón");
        assertEquals(12, secondItem.getQuantity());
        assertEquals("Pantalón", secondItem.getProductSpecification().getGarmentType());
        assertTrue(secondItem.getProductSpecification().isEmbroideryRequired());
        assertEquals(Map.of("M", 5, "L", 7), secondItem.getSizeBreakdowns().stream()
                .collect(Collectors.toMap(
                        com.magyen.platform.production.domain.SizeBreakdown::getSize,
                        com.magyen.platform.production.domain.SizeBreakdown::getQuantity
                )));
    }

    @Test
    void productionSnapshotRemainsUnchangedAfterCommercialItemMutation() {
        Order commercialOrder = persistConfirmedOrderWithTwoItems();
        UUID commercialItemId = commercialOrder.getItems().stream()
                .filter(item -> item.getProductName().equals("Camiseta Deportiva"))
                .findFirst()
                .orElseThrow()
                .getId();

        CreateProductionOrderResult result = createProductionOrderFromOrderUseCase.execute(
                new CreateProductionOrderCommand(
                        commercialOrder.getId(),
                        ProductionPriority.NORMAL,
                        null,
                        null,
                        "Independence verification"
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
                                "New personalization",
                                "Changed observations"
                        )
                )
        );
        replaceOrderItemSizesUseCase.execute(
                new ReplaceOrderItemSizesCommand(
                        commercialOrder.getId(),
                        commercialItemId,
                        List.of(
                                new SizeBreakdownCommand("XS", 2),
                                new SizeBreakdownCommand("XXL", 18)
                        )
                )
        );

        entityManager.flush();
        entityManager.clear();

        Order mutatedCommercialOrder = orderRepository.findById(commercialOrder.getId()).orElseThrow();
        OrderItem mutatedItem = mutatedCommercialOrder.getItems().stream()
                .filter(item -> item.getId().equals(commercialItemId))
                .findFirst()
                .orElseThrow();
        assertEquals("Polo", mutatedItem.getProductSpecification().getGarmentType());
        assertEquals(2, mutatedItem.getSizeBreakdowns().size());

        ProductionOrder productionOrder = reloadProductionOrder(result.productionOrderId());
        ProductionItem snapshotItem = findItemByProductName(productionOrder, "Camiseta Deportiva");

        assertEquals("Camiseta", snapshotItem.getProductSpecification().getGarmentType());
        assertEquals("Redondo", snapshotItem.getProductSpecification().getCollarType());
        assertEquals("Corta", snapshotItem.getProductSpecification().getSleeveType());
        assertEquals("Dry-fit", snapshotItem.getProductSpecification().getGarmentVariant());
        assertTrue(snapshotItem.getProductSpecification().isSublimationRequired());
        assertEquals("Full print", snapshotItem.getProductSpecification().getDecorationNotes());
        assertEquals("Roster completo", snapshotItem.getProductSpecification().getPersonalizationNotes());
        assertEquals("Prioridad alta", snapshotItem.getProductSpecification().getItemObservations());
        assertEquals(Map.of("S", 3, "M", 7, "L", 10), snapshotItem.getSizeBreakdowns().stream()
                .collect(Collectors.toMap(
                        com.magyen.platform.production.domain.SizeBreakdown::getSize,
                        com.magyen.platform.production.domain.SizeBreakdown::getQuantity
                )));
    }

    @Test
    void createsProductionItemWithEmptySizesWhenCommercialItemHasNoSizes() {
        Order commercialOrder = persistConfirmedOrder(
                List.of(OrderItem.reconstitute(
                        UUID.randomUUID(),
                        "Sudadera",
                        8,
                        "Algodón",
                        "Gris",
                        Money.of(new BigDecimal("55000")),
                        ProductSpecification.of(
                                "Sudadera",
                                "Capucha",
                                "Larga",
                                null,
                                false,
                                false,
                                true,
                                null,
                                false,
                                true,
                                false,
                                null,
                                "Sin tallas"
                        ),
                        List.of()
                ))
        );

        CreateProductionOrderResult result = createProductionOrderFromOrderUseCase.execute(
                new CreateProductionOrderCommand(
                        commercialOrder.getId(),
                        ProductionPriority.LOW,
                        null,
                        null,
                        null
                )
        );

        ProductionOrder productionOrder = reloadProductionOrder(result.productionOrderId());
        assertEquals(1, productionOrder.getItems().size());
        assertEquals("Sudadera", productionOrder.getItems().getFirst().getProductName());
        assertEquals(8, productionOrder.getItems().getFirst().getQuantity());
        assertTrue(productionOrder.getItems().getFirst().getSizeBreakdowns().isEmpty());
        assertEquals("Sudadera", productionOrder.getItems().getFirst().getProductSpecification().getGarmentType());
    }

    @Test
    void createsOneProductionItemPerCommercialOrderItem() {
        Order commercialOrder = persistConfirmedOrderWithTwoItems();

        CreateProductionOrderResult result = createProductionOrderFromOrderUseCase.execute(
                new CreateProductionOrderCommand(
                        commercialOrder.getId(),
                        ProductionPriority.NORMAL,
                        null,
                        null,
                        null
                )
        );

        ProductionOrder productionOrder = reloadProductionOrder(result.productionOrderId());
        assertEquals(commercialOrder.getItems().size(), productionOrder.getItems().size());
        assertEquals(2, productionOrder.getItems().size());
    }

    @Test
    void rejectsCreationWhenCommercialOrderIsNotConfirmed() {
        Order commercialOrder = persistConfirmedOrderWithTwoItems();
        commercialOrder.startProduction();
        orderRepository.save(commercialOrder);
        entityManager.flush();
        entityManager.clear();

        ProductionDomainException exception = assertThrows(
                ProductionDomainException.class,
                () -> createProductionOrderFromOrderUseCase.execute(
                        new CreateProductionOrderCommand(
                                commercialOrder.getId(),
                                ProductionPriority.NORMAL,
                                null,
                                null,
                                null
                        )
                )
        );

        assertTrue(exception.getMessage().contains("CONFIRMED"));
        assertTrue(productionOrderRepository.findByOrderId(commercialOrder.getId()).isEmpty());
    }

    @Test
    void rejectsSecondCreationForSameCommercialOrder() {
        Order commercialOrder = persistConfirmedOrderWithTwoItems();

        CreateProductionOrderResult firstResult = createProductionOrderFromOrderUseCase.execute(
                new CreateProductionOrderCommand(
                        commercialOrder.getId(),
                        ProductionPriority.NORMAL,
                        null,
                        null,
                        "First creation"
                )
        );

        ProductionOrderAlreadyExistsException exception = assertThrows(
                ProductionOrderAlreadyExistsException.class,
                () -> createProductionOrderFromOrderUseCase.execute(
                        new CreateProductionOrderCommand(
                                commercialOrder.getId(),
                                ProductionPriority.URGENT,
                                null,
                                null,
                                "Duplicate creation"
                        )
                )
        );

        assertEquals(
                ProductionOrderAlreadyExistsException.DEFAULT_MESSAGE,
                exception.getMessage()
        );

        ProductionOrder firstProductionOrder = reloadProductionOrder(firstResult.productionOrderId());
        assertEquals(ProductionStatus.CREATED, firstProductionOrder.getStatus());
        assertEquals(2, firstProductionOrder.getItems().size());
        assertEquals(firstResult.productionOrderId(), productionOrderRepository.findByOrderId(commercialOrder.getId())
                .orElseThrow()
                .getId());
    }

    @Test
    void createdProductionOrderWithSnapshotSupportsExistingLifecycle() {
        Order commercialOrder = persistConfirmedOrderWithTwoItems();
        CreateProductionOrderResult result = createProductionOrderFromOrderUseCase.execute(
                new CreateProductionOrderCommand(
                        commercialOrder.getId(),
                        ProductionPriority.NORMAL,
                        null,
                        null,
                        "Lifecycle with snapshot"
                )
        );

        UUID productionOrderId = result.productionOrderId();
        LocalDate plannedStart = LocalDate.now().plusDays(1);
        LocalDate plannedEnd = LocalDate.now().plusDays(8);

        var addResult = addProductionOperationUseCase.execute(
                new AddProductionOperationCommand(
                        productionOrderId,
                        ProductionOperationType.CUTTING,
                        plannedStart,
                        plannedEnd,
                        "Cutting"
                )
        );

        planProductionOrderUseCase.execute(
                new PlanProductionOrderCommand(
                        productionOrderId,
                        plannedStart,
                        plannedEnd,
                        ProductionPriority.HIGH
                )
        );
        startProductionOrderUseCase.execute(new StartProductionOrderCommand(productionOrderId));
        startProductionOperationUseCase.execute(
                new StartProductionOperationCommand(productionOrderId, addResult.operationId())
        );
        completeProductionOperationUseCase.execute(
                new CompleteProductionOperationCommand(productionOrderId, addResult.operationId())
        );
        completeProductionOrderUseCase.execute(new CompleteProductionOrderCommand(productionOrderId));

        ProductionOrder completed = reloadProductionOrder(productionOrderId);
        assertEquals(ProductionStatus.COMPLETED, completed.getStatus());
        assertEquals(2, completed.getItems().size());
        assertEquals("Camiseta Deportiva", findItemByProductName(completed, "Camiseta Deportiva").getProductName());
        assertEquals(Map.of("S", 3, "M", 7, "L", 10), findItemByProductName(completed, "Camiseta Deportiva")
                .getSizeBreakdowns()
                .stream()
                .collect(Collectors.toMap(
                        com.magyen.platform.production.domain.SizeBreakdown::getSize,
                        com.magyen.platform.production.domain.SizeBreakdown::getQuantity
                )));
    }

    @Test
    void rejectsCreationWhenCommercialOrderDoesNotExist() {
        UUID missingOrderId = UUID.randomUUID();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createProductionOrderFromOrderUseCase.execute(
                        new CreateProductionOrderCommand(
                                missingOrderId,
                                ProductionPriority.NORMAL,
                                null,
                                null,
                                null
                        )
                )
        );

        assertTrue(exception.getMessage().contains("Order not found"));
    }

    private Order persistConfirmedOrderWithTwoItems() {
        OrderItem firstItem = OrderItem.reconstitute(
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

        OrderItem secondItem = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Pantalón",
                12,
                "Microfibra",
                "Negro",
                Money.of(new BigDecimal("30000")),
                ProductSpecification.of(
                        "Pantalón",
                        null,
                        null,
                        "Slim",
                        false,
                        true,
                        false,
                        "Bordado lateral",
                        false,
                        false,
                        true,
                        null,
                        null
                ),
                List.of(
                        SizeBreakdown.create("M", 5),
                        SizeBreakdown.create("L", 7)
                )
        );

        return persistConfirmedOrder(List.of(firstItem, secondItem));
    }

    private Order persistConfirmedOrder(List<OrderItem> items) {
        LocalDate today = LocalDate.now();
        Order order = Order.create(
                OrderNumber.of("ORD-PO-" + UUID.randomUUID().toString().substring(0, 8)),
                UUID.randomUUID(),
                UUID.randomUUID(),
                today,
                DeliveryCommitment.of(today.plusDays(14)),
                "Creation Tester",
                "Commercial order for production creation",
                items
        );
        Order saved = orderRepository.save(order);
        entityManager.flush();
        entityManager.clear();
        return orderRepository.findById(saved.getId()).orElseThrow();
    }

    private ProductionOrder reloadProductionOrder(UUID productionOrderId) {
        entityManager.flush();
        entityManager.clear();
        return productionOrderRepository.findById(productionOrderId)
                .orElseThrow(() -> new IllegalStateException(
                        "Production order not found after reload: " + productionOrderId
                ));
    }

    private static ProductionItem findItemByProductName(ProductionOrder productionOrder, String productName) {
        return productionOrder.getItems().stream()
                .filter(item -> item.getProductName().equals(productName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Production item not found: " + productName));
    }
}
