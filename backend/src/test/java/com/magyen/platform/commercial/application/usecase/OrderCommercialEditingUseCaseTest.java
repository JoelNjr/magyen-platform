package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.AddOrderItemCommand;
import com.magyen.platform.commercial.application.dto.AddQuotationItemCommand;
import com.magyen.platform.commercial.application.dto.ApplyOrderDiscountCommand;
import com.magyen.platform.commercial.application.dto.ApproveQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateCustomerCommand;
import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationResult;
import com.magyen.platform.commercial.application.dto.CreateQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateQuotationResult;
import com.magyen.platform.commercial.application.dto.ProductSpecificationCommand;
import com.magyen.platform.commercial.application.dto.RemoveOrderItemCommand;
import com.magyen.platform.commercial.application.dto.UpdateOrderItemCommand;
import com.magyen.platform.commercial.application.dto.UpdateOrderItemProductSpecificationCommand;
import com.magyen.platform.commercial.domain.DeliveryCommitment;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderItem;
import com.magyen.platform.commercial.domain.OrderNumber;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.OrderStatus;
import com.magyen.platform.commercial.domain.ProductSpecification;
import com.magyen.platform.commercial.domain.Quotation;
import com.magyen.platform.commercial.domain.QuotationRepository;
import com.magyen.platform.commercial.domain.SizeBreakdown;
import com.magyen.platform.commercial.domain.exception.OrderDomainException;
import com.magyen.platform.finance.application.dto.RegisterPaymentCommand;
import com.magyen.platform.finance.application.usecase.CreatePayrollEmployeeUseCase;
import com.magyen.platform.finance.application.usecase.RegisterPaymentUseCase;
import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.Payment;
import com.magyen.platform.finance.domain.PaymentRepository;
import com.magyen.platform.production.application.dto.CreateProductionOrderCommand;
import com.magyen.platform.production.application.dto.CreateProductionOrderResult;
import com.magyen.platform.production.application.usecase.CreateProductionOrderFromOrderUseCase;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class OrderCommercialEditingUseCaseTest {

    private static final LocalDate QUOTATION_DATE = LocalDate.of(2098, 4, 1);

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
    private AddOrderItemUseCase addOrderItemUseCase;
    @Autowired
    private UpdateOrderItemUseCase updateOrderItemUseCase;
    @Autowired
    private RemoveOrderItemUseCase removeOrderItemUseCase;
    @Autowired
    private ApplyOrderDiscountUseCase applyOrderDiscountUseCase;
    @Autowired
    private UpdateOrderItemProductSpecificationUseCase updateOrderItemProductSpecificationUseCase;
    @Autowired
    private CreateProductionOrderFromOrderUseCase createProductionOrderFromOrderUseCase;
    @Autowired
    private RegisterPaymentUseCase registerPaymentUseCase;
    @Autowired
    private CreatePayrollEmployeeUseCase createPayrollEmployeeUseCase;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private QuotationRepository quotationRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;
    @Autowired
    private ProductionOrderRepository productionOrderRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    void addUpdateRemoveAndDiscountPreserveTotalsNumberTraceabilityAndPayments() {
        PreparedOrder prepared = createTracedOrder("Camiseta local", 10, "20000");
        UUID quotationItemId = prepared.order().getItems().getFirst().getQuotationItemId();
        String orderNumber = prepared.order().getOrderNumber().getValue();
        UUID quotationId = prepared.quotationId();

        addOrderItemUseCase.execute(new AddOrderItemCommand(
                prepared.order().getId(),
                "Pantaloneta extra",
                2,
                "Hydrotech",
                null,
                "Negro",
                new BigDecimal("15000"),
                null
        ));

        entityManager.flush();
        entityManager.clear();
        Order afterAdd = orderRepository.findById(prepared.order().getId()).orElseThrow();
        OrderItem manual = afterAdd.getItems().stream()
                .filter(item -> "Pantaloneta extra".equals(item.getProductName()))
                .findFirst()
                .orElseThrow();
        assertNull(manual.getQuotationItemId());
        assertEquals(Money.of(new BigDecimal("230000")), afterAdd.getTotal());
        assertEquals(orderNumber, afterAdd.getOrderNumber().getValue());

        UUID tracedItemId = afterAdd.getItems().stream()
                .filter(item -> quotationItemId.equals(item.getQuotationItemId()))
                .findFirst()
                .orElseThrow()
                .getId();
        updateOrderItemUseCase.execute(new UpdateOrderItemCommand(
                afterAdd.getId(),
                tracedItemId,
                12,
                new BigDecimal("20000")
        ));
        entityManager.flush();
        entityManager.clear();
        Order afterUpdate = orderRepository.findById(afterAdd.getId()).orElseThrow();
        OrderItem traced = afterUpdate.getItems().stream()
                .filter(item -> quotationItemId.equals(item.getQuotationItemId()))
                .findFirst()
                .orElseThrow();
        assertEquals(quotationItemId, traced.getQuotationItemId());
        assertEquals(12, traced.getQuantity());
        assertEquals(Money.of(new BigDecimal("270000")), afterUpdate.getTotal());

        applyOrderDiscountUseCase.execute(new ApplyOrderDiscountCommand(
                afterUpdate.getId(),
                new BigDecimal("20000")
        ));
        entityManager.flush();
        entityManager.clear();
        Order afterDiscount = orderRepository.findById(afterUpdate.getId()).orElseThrow();
        assertEquals(Money.of(new BigDecimal("250000")), afterDiscount.getTotal());
        assertEquals(
                afterDiscount.getSubtotal().subtract(afterDiscount.getDiscount()),
                afterDiscount.getTotal()
        );

        UUID manualItemId = afterDiscount.getItems().stream()
                .filter(item -> "Pantaloneta extra".equals(item.getProductName()))
                .findFirst()
                .orElseThrow()
                .getId();
        removeOrderItemUseCase.execute(new RemoveOrderItemCommand(afterDiscount.getId(), manualItemId));
        entityManager.flush();
        entityManager.clear();

        Order afterRemove = orderRepository.findById(afterDiscount.getId()).orElseThrow();
        Quotation quotation = quotationRepository.findById(quotationId).orElseThrow();
        assertEquals(1, afterRemove.getItems().size());
        assertEquals(quotationItemId, afterRemove.getItems().getFirst().getQuotationItemId());
        assertEquals(10, quotation.getItems().getFirst().getQuantity());
        assertEquals(orderNumber, afterRemove.getOrderNumber().getValue());
        assertTrue(paymentRepository.findByOrderId(afterRemove.getId()).isEmpty());
    }

    @Test
    void paymentFloorRejectsTotalBelowPaidAndLeavesFinanceUntouched() {
        PreparedOrder prepared = createTracedOrder("Camiseta pagada", 10, "100000");
        registerPaymentUseCase.execute(new RegisterPaymentCommand(
                prepared.order().getId(),
                new BigDecimal("1000000.00"),
                QUOTATION_DATE.plusDays(3),
                "Pago completo"
        ));
        entityManager.flush();
        List<Payment> paymentsBefore = paymentRepository.findByOrderId(prepared.order().getId());
        UUID paymentId = paymentsBefore.getFirst().getId();
        FinancialTransaction transactionBefore = financialTransactionRepository
                .findBySourceTypeAndSourceId(
                        com.magyen.platform.finance.domain.FinancialTransactionSourceType.COMMERCIAL_ORDER,
                        paymentId
                )
                .orElseThrow();
        BigDecimal paidAmount = paymentsBefore.getFirst().getAmount().getValue();
        BigDecimal transactionAmount = transactionBefore.getAmount().getValue();

        updateOrderItemUseCase.execute(new UpdateOrderItemCommand(
                prepared.order().getId(),
                prepared.order().getItems().getFirst().getId(),
                12,
                new BigDecimal("100000")
        ));
        entityManager.flush();
        entityManager.clear();
        Order increased = orderRepository.findById(prepared.order().getId()).orElseThrow();
        assertEquals(Money.of(new BigDecimal("1200000")), increased.getTotal());
        assertEquals(1, paymentRepository.findByOrderId(increased.getId()).size());
        assertEquals(paidAmount, paymentRepository.findByOrderId(increased.getId()).getFirst().getAmount().getValue());

        OrderDomainException rejected = assertThrows(
                OrderDomainException.class,
                () -> updateOrderItemUseCase.execute(new UpdateOrderItemCommand(
                        increased.getId(),
                        increased.getItems().getFirst().getId(),
                        8,
                        new BigDecimal("100000")
                ))
        );
        assertTrue(rejected.getMessage().contains("already paid"));

        entityManager.flush();
        entityManager.clear();
        Order unchanged = orderRepository.findById(increased.getId()).orElseThrow();
        assertEquals(12, unchanged.getItems().getFirst().getQuantity());
        assertEquals(Money.of(new BigDecimal("1200000")), unchanged.getTotal());
        assertEquals(1, paymentRepository.findByOrderId(unchanged.getId()).size());
        FinancialTransaction transactionAfter = financialTransactionRepository
                .findBySourceTypeAndSourceId(
                        com.magyen.platform.finance.domain.FinancialTransactionSourceType.COMMERCIAL_ORDER,
                        paymentId
                )
                .orElseThrow();
        assertEquals(transactionAmount, transactionAfter.getAmount().getValue());
        assertEquals(transactionBefore.getId(), transactionAfter.getId());
    }

    @Test
    void editingOrderDoesNotMutateExistingProductionSnapshot() {
        Order historical = persistLegacyOrder();
        CreateProductionOrderResult productionResult = createProductionOrderFromOrderUseCase.execute(
                new CreateProductionOrderCommand(
                        historical.getId(),
                        ProductionPriority.NORMAL,
                        null,
                        null,
                        "Snapshot previo"
                )
        );
        entityManager.flush();
        entityManager.clear();
        ProductionOrder snapshotBefore = productionOrderRepository.findById(productionResult.productionOrderId())
                .orElseThrow();
        int snapshotQuantity = snapshotBefore.getItems().getFirst().getQuantity();

        updateOrderItemUseCase.execute(new UpdateOrderItemCommand(
                historical.getId(),
                historical.getItems().getFirst().getId(),
                22,
                new BigDecimal("45000")
        ));
        entityManager.flush();
        entityManager.clear();

        Order edited = orderRepository.findById(historical.getId()).orElseThrow();
        ProductionOrder snapshotAfter = productionOrderRepository.findById(productionResult.productionOrderId())
                .orElseThrow();
        assertNull(edited.getItems().getFirst().getQuotationItemId());
        assertEquals(22, edited.getItems().getFirst().getQuantity());
        assertEquals(snapshotQuantity, snapshotAfter.getItems().getFirst().getQuantity());
        assertEquals(20, snapshotAfter.getItems().getFirst().getQuantity());
        assertEquals(historical.getOrderNumber().getValue(), edited.getOrderNumber().getValue());
    }

    @Test
    void specificationUpdateIsRejectedAfterDelivery() {
        Order delivered = persistOrderWithStatus(OrderStatus.DELIVERED);
        OrderDomainException exception = assertThrows(
                OrderDomainException.class,
                () -> updateOrderItemProductSpecificationUseCase.execute(
                        new UpdateOrderItemProductSpecificationCommand(
                                delivered.getId(),
                                delivered.getItems().getFirst().getId(),
                                new ProductSpecificationCommand(
                                        "Camiseta",
                                        "Redondo",
                                        "Manga corta sisa",
                                        true,
                                        true,
                                        false,
                                        false,
                                        null,
                                        false,
                                        false,
                                        false,
                                        null,
                                        null
                                )
                        )
                )
        );
        assertTrue(exception.getMessage().contains("DELIVERED"));
    }

    private PreparedOrder createTracedOrder(String productName, int quantity, String unitPrice) {
        UUID sellerId = FixedSellerEmployeeFixture.create(
                createPayrollEmployeeUseCase,
                "Seller-edit-" + UUID.randomUUID().toString().substring(0, 8)
        );
        var customer = createCustomerUseCase.execute(
                new CreateCustomerCommand("Cliente edit " + UUID.randomUUID())
        );
        CreateQuotationResult quotation = createQuotationUseCase.execute(new CreateQuotationCommand(
                customer.customerId(),
                QUOTATION_DATE.plusDays(10),
                sellerId,
                productName,
                QUOTATION_DATE
        ));
        addQuotationItemUseCase.execute(new AddQuotationItemCommand(
                quotation.quotationId(),
                productName,
                quantity,
                "Sudáfrica",
                "Blanco",
                new BigDecimal(unitPrice),
                null
        ));
        approveQuotationUseCase.execute(new ApproveQuotationCommand(quotation.quotationId()));
        CreateOrderFromQuotationResult created = createOrderFromQuotationUseCase.execute(
                new CreateOrderFromQuotationCommand(
                        quotation.quotationId(),
                        productName,
                        QUOTATION_DATE.plusDays(2),
                        QUOTATION_DATE.plusDays(12),
                        null
                )
        );
        entityManager.flush();
        entityManager.clear();
        return new PreparedOrder(
                quotation.quotationId(),
                orderRepository.findById(created.orderId()).orElseThrow()
        );
    }

    private Order persistLegacyOrder() {
        OrderItem item = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Camiseta histórica",
                20,
                "Hydrotech",
                "Azul",
                Money.of(new BigDecimal("45000")),
                ProductSpecification.empty(),
                List.of(SizeBreakdown.create("S", 5), SizeBreakdown.create("M", 5))
        );
        return persistOrder(OrderStatus.CONFIRMED, item);
    }

    private Order persistOrderWithStatus(OrderStatus status) {
        return persistOrder(status, OrderItem.reconstitute(
                UUID.randomUUID(),
                "Camiseta",
                10,
                "Sudáfrica",
                "Blanco",
                Money.of(new BigDecimal("20000")),
                ProductSpecification.empty(),
                List.of()
        ));
    }

    private Order persistOrder(OrderStatus status, OrderItem item) {
        LocalDate today = LocalDate.of(2098, 4, 10);
        Order created = Order.create(
                OrderNumber.of("ORD-EDU-" + UUID.randomUUID().toString().substring(0, 8)),
                UUID.randomUUID(),
                UUID.randomUUID(),
                today,
                DeliveryCommitment.of(today.plusDays(10)),
                UUID.randomUUID(),
                "Edición comercial",
                List.of(item)
        );
        if (status != OrderStatus.CONFIRMED) {
            created = Order.reconstitute(
                    created.getId(),
                    created.getOrderNumber(),
                    created.getCustomerId(),
                    created.getQuotationId(),
                    created.getConfirmationDate(),
                    status,
                    created.getDeliveryCommitment(),
                    created.getPaymentSummary(),
                    created.getSellerId(),
                    created.getObservations(),
                    created.getDescription(),
                    created.getItems(),
                    created.getDiscount()
            );
        }
        Order saved = orderRepository.save(created);
        entityManager.flush();
        entityManager.clear();
        return orderRepository.findById(saved.getId()).orElseThrow();
    }

    private record PreparedOrder(UUID quotationId, Order order) {
    }
}
