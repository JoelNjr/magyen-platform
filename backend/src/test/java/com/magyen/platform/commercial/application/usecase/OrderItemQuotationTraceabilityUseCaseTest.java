package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.AddQuotationItemCommand;
import com.magyen.platform.commercial.application.dto.AddQuotationItemResult;
import com.magyen.platform.commercial.application.dto.ApproveQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateCustomerCommand;
import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationResult;
import com.magyen.platform.commercial.application.dto.CreateQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateQuotationResult;
import com.magyen.platform.commercial.application.dto.ProductSpecificationCommand;
import com.magyen.platform.commercial.domain.DeliveryCommitment;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderItem;
import com.magyen.platform.commercial.domain.OrderNumber;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.ProductSpecification;
import com.magyen.platform.commercial.domain.QuotationRepository;
import com.magyen.platform.commercial.domain.SizeBreakdown;
import com.magyen.platform.commercial.domain.exception.OrderAlreadyExistsForQuotationException;
import com.magyen.platform.finance.application.usecase.CreatePayrollEmployeeUseCase;
import com.magyen.platform.finance.domain.PaymentRepository;
import com.magyen.platform.production.application.dto.CreateProductionOrderCommand;
import com.magyen.platform.production.application.dto.CreateProductionOrderResult;
import com.magyen.platform.production.application.usecase.CreateProductionOrderFromOrderUseCase;
import com.magyen.platform.production.domain.ProductionItem;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import com.magyen.platform.production.domain.ProductionPriority;
import com.magyen.platform.shared.domain.Money;
import com.magyen.platform.shared.testsupport.FixedSellerEmployeeFixture;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Trazabilidad persistida QuotationItem → OrderItem para órdenes nuevas.
 * No habilita edición de cotización, no crea pagos y no muta producción histórica.
 */
@SpringBootTest
@Transactional
class OrderItemQuotationTraceabilityUseCaseTest {

    private static final LocalDate QUOTATION_DATE = LocalDate.of(2097, 3, 1);

    @Autowired
    private CreateCustomerUseCase createCustomerUseCase;

    @Autowired
    private CreateQuotationUseCase createQuotationUseCase;

    @Autowired
    private AddQuotationItemUseCase addQuotationItemUseCase;

    @Autowired
    private ApproveQuotationUseCase approveQuotationUseCase;

    @Autowired
    private CreateOrderFromQuotationUseCase createOrderFromQuotationUseCase;

    @Autowired
    private CreateProductionOrderFromOrderUseCase createProductionOrderFromOrderUseCase;

    @Autowired
    private CreatePayrollEmployeeUseCase createPayrollEmployeeUseCase;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private QuotationRepository quotationRepository;

    @Autowired
    private ProductionOrderRepository productionOrderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void newOrderFromQuotationPersistsOneToOneQuotationItemTraceability() {
        UUID sellerId = seller();
        CreateQuotationResult quotation = createQuotation(sellerId, "Trace new");
        AddQuotationItemResult firstItem = addSpecifiedItem(
                quotation.quotationId(),
                "Camiseta local",
                10,
                "Sudáfrica",
                "Blanco",
                "45000"
        );
        AddQuotationItemResult secondItem = addItem(
                quotation.quotationId(),
                "Pantaloneta",
                4,
                "Hydrotech",
                "Negro",
                "30000"
        );
        approveQuotationUseCase.execute(new ApproveQuotationCommand(quotation.quotationId()));

        CreateOrderFromQuotationResult created = createOrderFromQuotationUseCase.execute(
                new CreateOrderFromQuotationCommand(
                        quotation.quotationId(),
                        "Pedido trazable",
                        QUOTATION_DATE.plusDays(2),
                        QUOTATION_DATE.plusDays(12),
                        "Observación comercial"
                )
        );

        entityManager.flush();
        entityManager.clear();

        Order reloaded = orderRepository.findById(created.orderId()).orElseThrow();
        var persistedQuotation = quotationRepository.findById(quotation.quotationId()).orElseThrow();

        assertEquals(quotation.quotationId(), reloaded.getQuotationId());
        assertEquals(Long.toString(persistedQuotation.getQuotationNumber().getValue()), reloaded.getOrderNumber().getValue());
        assertEquals(persistedQuotation.getTotal(), reloaded.getTotal());
        assertEquals(2, reloaded.getItems().size());
        assertEquals(firstItem.itemId(), findByProductName(reloaded, "Camiseta local").getQuotationItemId());
        assertEquals(secondItem.itemId(), findByProductName(reloaded, "Pantaloneta").getQuotationItemId());
        assertEquals(10, findByProductName(reloaded, "Camiseta local").getQuantity());
        assertEquals(Money.of(new BigDecimal("45000")), findByProductName(reloaded, "Camiseta local").getUnitPrice());
        assertEquals("Camiseta", findByProductName(reloaded, "Camiseta local").getProductSpecification().getGarmentType());
        assertEquals("Redondo", findByProductName(reloaded, "Camiseta local").getProductSpecification().getCollarType());
        assertTrue(findByProductName(reloaded, "Camiseta local").getSizeBreakdowns().isEmpty());
        assertTrue(findByProductName(reloaded, "Pantaloneta").getProductSpecification().isEmpty());
        assertTrue(paymentRepository.findByOrderId(reloaded.getId()).isEmpty());
        Number commercialPaymentTransactions = (Number) entityManager.createNativeQuery(
                "select count(*) from financial_transactions ft"
                        + " inner join payments p on p.id = ft.source_id"
                        + " where ft.source_type = 'COMMERCIAL_ORDER' and p.order_id = :orderId"
        ).setParameter("orderId", reloaded.getId()).getSingleResult();
        assertEquals(0L, commercialPaymentTransactions.longValue());

        assertThrows(
                OrderAlreadyExistsForQuotationException.class,
                () -> createOrderFromQuotationUseCase.execute(new CreateOrderFromQuotationCommand(
                        quotation.quotationId(),
                        "Duplicado",
                        QUOTATION_DATE.plusDays(3),
                        QUOTATION_DATE.plusDays(13),
                        null
                ))
        );
    }

    @Test
    void historicalNullQuotationItemIdRemainsLoadableAndDoesNotAffectExistingProduction() {
        Order historical = persistHistoricalOrderWithSizes();
        UUID historicalItemId = historical.getItems().getFirst().getId();
        String historicalProductName = historical.getItems().getFirst().getProductName();
        Money historicalTotal = historical.getTotal();
        UUID historicalQuotationId = historical.getQuotationId();

        CreateProductionOrderResult productionResult = createProductionOrderFromOrderUseCase.execute(
                new CreateProductionOrderCommand(
                        historical.getId(),
                        ProductionPriority.NORMAL,
                        null,
                        null,
                        "Snapshot histórico"
                )
        );
        entityManager.flush();
        entityManager.clear();

        ProductionOrder originalSnapshot = productionOrderRepository.findById(productionResult.productionOrderId())
                .orElseThrow();
        int originalItemCount = originalSnapshot.getItems().size();
        String originalSnapshotName = originalSnapshot.getItems().getFirst().getProductName();
        int originalSnapshotQuantity = originalSnapshot.getItems().getFirst().getQuantity();

        CreateQuotationResult quotation = createQuotation(seller(), "Trace after historical");
        addItem(quotation.quotationId(), "Nuevo producto", 2, "Sudáfrica", "Blanco", "10000");
        approveQuotationUseCase.execute(new ApproveQuotationCommand(quotation.quotationId()));
        createOrderFromQuotationUseCase.execute(new CreateOrderFromQuotationCommand(
                quotation.quotationId(),
                "Pedido nuevo",
                QUOTATION_DATE.plusDays(4),
                QUOTATION_DATE.plusDays(14),
                null
        ));

        entityManager.flush();
        entityManager.clear();

        Order reloadedHistorical = orderRepository.findById(historical.getId()).orElseThrow();
        OrderItem reloadedHistoricalItem = reloadedHistorical.getItems().stream()
                .filter(item -> item.getId().equals(historicalItemId))
                .findFirst()
                .orElseThrow();

        assertNull(reloadedHistoricalItem.getQuotationItemId());
        assertEquals(historicalProductName, reloadedHistoricalItem.getProductName());
        assertEquals(historicalTotal, reloadedHistorical.getTotal());
        assertEquals(historicalQuotationId, reloadedHistorical.getQuotationId());
        assertEquals(20, reloadedHistoricalItem.getAssignedSizeQuantity());
        assertEquals("Camiseta", reloadedHistoricalItem.getProductSpecification().getGarmentType());

        ProductionOrder reloadedProduction = productionOrderRepository.findById(productionResult.productionOrderId())
                .orElseThrow();
        assertEquals(originalItemCount, reloadedProduction.getItems().size());
        assertEquals(originalSnapshotName, reloadedProduction.getItems().getFirst().getProductName());
        assertEquals(originalSnapshotQuantity, reloadedProduction.getItems().getFirst().getQuantity());
        assertEquals(Map.of("S", 3, "M", 7, "L", 10), sizesOf(reloadedProduction.getItems().getFirst()));
        assertTrue(paymentRepository.findByOrderId(historical.getId()).isEmpty());
    }

    @Test
    void productionCreationFromTracedOrderIgnoresQuotationItemId() {
        UUID sellerId = seller();
        CreateQuotationResult quotation = createQuotation(sellerId, "Trace production");
        addSpecifiedItem(quotation.quotationId(), "Camiseta production", 8, "Sudáfrica", "Blanco", "25000");
        approveQuotationUseCase.execute(new ApproveQuotationCommand(quotation.quotationId()));
        CreateOrderFromQuotationResult order = createOrderFromQuotationUseCase.execute(
                new CreateOrderFromQuotationCommand(
                        quotation.quotationId(),
                        "Pedido production",
                        QUOTATION_DATE.plusDays(5),
                        QUOTATION_DATE.plusDays(15),
                        null
                )
        );

        CreateProductionOrderResult productionResult = createProductionOrderFromOrderUseCase.execute(
                new CreateProductionOrderCommand(
                        order.orderId(),
                        ProductionPriority.HIGH,
                        QUOTATION_DATE.plusDays(6),
                        QUOTATION_DATE.plusDays(16),
                        "From traced order"
                )
        );

        entityManager.flush();
        entityManager.clear();

        Order commercial = orderRepository.findById(order.orderId()).orElseThrow();
        ProductionOrder production = productionOrderRepository.findById(productionResult.productionOrderId())
                .orElseThrow();
        ProductionItem productionItem = production.getItems().getFirst();
        OrderItem commercialItem = commercial.getItems().getFirst();

        assertEquals(commercialItem.getQuotationItemId(), quotationRepository.findById(quotation.quotationId())
                .orElseThrow()
                .getItems()
                .getFirst()
                .getId());
        assertEquals(commercial.getId(), production.getOrderId());
        assertEquals(commercialItem.getProductName(), productionItem.getProductName());
        assertEquals(commercialItem.getQuantity(), productionItem.getQuantity());
        assertEquals(
                commercialItem.getProductSpecification().getGarmentType(),
                productionItem.getProductSpecification().getGarmentType()
        );
        assertTrue(productionItem.getSizeBreakdowns().isEmpty());
        assertTrue(paymentRepository.findByOrderId(commercial.getId()).isEmpty());
    }

    private Order persistHistoricalOrderWithSizes() {
        OrderItem item = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Camiseta histórica",
                20,
                "Hydrotech",
                "Azul",
                Money.of(new BigDecimal("45000")),
                ProductSpecification.of(
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
                        "Roster",
                        "Histórico"
                ),
                List.of(
                        SizeBreakdown.create("S", 3),
                        SizeBreakdown.create("M", 7),
                        SizeBreakdown.create("L", 10)
                )
        );

        LocalDate today = LocalDate.of(2097, 2, 1);
        Order saved = orderRepository.save(Order.create(
                OrderNumber.of("ORD-HIST-" + UUID.randomUUID().toString().substring(0, 8)),
                UUID.randomUUID(),
                UUID.randomUUID(),
                today,
                DeliveryCommitment.of(today.plusDays(10)),
                UUID.randomUUID(),
                "Orden histórica",
                List.of(item)
        ));
        entityManager.flush();
        entityManager.clear();
        return orderRepository.findById(saved.getId()).orElseThrow();
    }

    private UUID seller() {
        return FixedSellerEmployeeFixture.create(
                createPayrollEmployeeUseCase,
                "Seller-trace-" + UUID.randomUUID().toString().substring(0, 8)
        );
    }

    private CreateQuotationResult createQuotation(UUID sellerId, String name) {
        var customer = createCustomerUseCase.execute(new CreateCustomerCommand(name + " " + UUID.randomUUID()));
        return createQuotationUseCase.execute(new CreateQuotationCommand(
                customer.customerId(),
                QUOTATION_DATE.plusDays(10),
                sellerId,
                name,
                QUOTATION_DATE
        ));
    }

    private AddQuotationItemResult addItem(
            UUID quotationId,
            String productName,
            int quantity,
            String fabric,
            String color,
            String unitPrice
    ) {
        return addQuotationItemUseCase.execute(new AddQuotationItemCommand(
                quotationId,
                productName,
                quantity,
                fabric,
                color,
                new BigDecimal(unitPrice),
                null
        ));
    }

    private AddQuotationItemResult addSpecifiedItem(
            UUID quotationId,
            String productName,
            int quantity,
            String fabric,
            String color,
            String unitPrice
    ) {
        return addQuotationItemUseCase.execute(new AddQuotationItemCommand(
                quotationId,
                productName,
                quantity,
                fabric,
                color,
                new BigDecimal(unitPrice),
                new ProductSpecificationCommand(
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
                        "Prioridad"
                )
        ));
    }

    private static OrderItem findByProductName(Order order, String productName) {
        return order.getItems().stream()
                .filter(item -> item.getProductName().equals(productName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Order item not found: " + productName));
    }

    private static Map<String, Integer> sizesOf(ProductionItem item) {
        return item.getSizeBreakdowns().stream()
                .collect(Collectors.toMap(
                        com.magyen.platform.production.domain.SizeBreakdown::getSize,
                        com.magyen.platform.production.domain.SizeBreakdown::getQuantity
                ));
    }
}
