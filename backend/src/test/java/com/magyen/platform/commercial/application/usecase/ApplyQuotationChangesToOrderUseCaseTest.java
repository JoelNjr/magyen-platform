package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.AddOrderItemCommand;
import com.magyen.platform.commercial.application.dto.AddQuotationItemCommand;
import com.magyen.platform.commercial.application.dto.ApplyQuotationChangesToOrderCommand;
import com.magyen.platform.commercial.application.dto.ApproveQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateCustomerCommand;
import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationResult;
import com.magyen.platform.commercial.application.dto.CreateQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateQuotationResult;
import com.magyen.platform.commercial.application.dto.PreviewQuotationOrderSynchronizationQuery;
import com.magyen.platform.commercial.application.dto.PreviewQuotationOrderSynchronizationResult;
import com.magyen.platform.commercial.application.dto.ProductSpecificationCommand;
import com.magyen.platform.commercial.application.dto.RemoveQuotationItemCommand;
import com.magyen.platform.commercial.application.dto.ReplaceOrderItemSizesCommand;
import com.magyen.platform.commercial.application.dto.SizeBreakdownCommand;
import com.magyen.platform.commercial.application.dto.UpdateQuotationItemCommand;
import com.magyen.platform.commercial.domain.DeliveryCommitment;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderItem;
import com.magyen.platform.commercial.domain.OrderNumber;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.OrderStatus;
import com.magyen.platform.commercial.domain.ProductSpecification;
import com.magyen.platform.commercial.domain.Quotation;
import com.magyen.platform.commercial.domain.QuotationRepository;
import com.magyen.platform.commercial.domain.exception.OrderDomainException;
import com.magyen.platform.finance.application.dto.RegisterPaymentCommand;
import com.magyen.platform.finance.application.usecase.CreatePayrollEmployeeUseCase;
import com.magyen.platform.finance.application.usecase.RegisterPaymentUseCase;
import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class ApplyQuotationChangesToOrderUseCaseTest {

    private static final LocalDate QUOTATION_DATE = LocalDate.of(2098, 5, 1);

    @Autowired
    private CreateCustomerUseCase createCustomerUseCase;
    @Autowired
    private CreateQuotationUseCase createQuotationUseCase;
    @Autowired
    private AddQuotationItemUseCase addQuotationItemUseCase;
    @Autowired
    private UpdateQuotationItemUseCase updateQuotationItemUseCase;
    @Autowired
    private RemoveQuotationItemUseCase removeQuotationItemUseCase;
    @Autowired
    private ApproveQuotationUseCase approveQuotationUseCase;
    @Autowired
    private CreateOrderFromQuotationUseCase createOrderFromQuotationUseCase;
    @Autowired
    private AddOrderItemUseCase addOrderItemUseCase;
    @Autowired
    private ReplaceOrderItemSizesUseCase replaceOrderItemSizesUseCase;
    @Autowired
    private PreviewQuotationOrderSynchronizationUseCase previewQuotationOrderSynchronizationUseCase;
    @Autowired
    private ApplyQuotationChangesToOrderUseCase applyQuotationChangesToOrderUseCase;
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
    void previewWithoutOrderIsUnavailableAndQuotationRemainsEditable() {
        PreparedQuotation quotation = createApprovedQuotationWithoutOrder("Camiseta sola", 10, "20000");
        PreviewQuotationOrderSynchronizationResult preview =
                previewQuotationOrderSynchronizationUseCase.execute(
                        new PreviewQuotationOrderSynchronizationQuery(quotation.quotationId())
                );
        assertFalse(preview.orderExists());
        assertFalse(preview.applyAllowed());
        assertEquals(
                PreviewQuotationOrderSynchronizationUseCase.UNAVAILABLE_NO_ASSOCIATED_ORDER,
                preview.unavailableReason()
        );

        updateQuotationItemUseCase.execute(new UpdateQuotationItemCommand(
                quotation.quotationId(),
                quotation.itemId(),
                "Camiseta sola",
                12,
                "Sudáfrica",
                null,
                "Blanco",
                new BigDecimal("20000"),
                null
        ));
        entityManager.flush();
        entityManager.clear();
        Quotation updated = quotationRepository.findById(quotation.quotationId()).orElseThrow();
        assertEquals(12, updated.getItems().getFirst().getQuantity());
        assertTrue(orderRepository.findByQuotationId(quotation.quotationId()).isEmpty());
    }

    @Test
    void legacyOrderRejectsApplyAndLeavesOrderUnchanged() {
        PreparedQuotation prepared = createApprovedQuotationWithoutOrder("Legacy", 8, "20000");
        Quotation quotation = quotationRepository.findById(prepared.quotationId()).orElseThrow();
        Order legacy = persistLegacyOrderForQuotation(quotation);
        PreviewQuotationOrderSynchronizationResult preview =
                previewQuotationOrderSynchronizationUseCase.execute(
                        new PreviewQuotationOrderSynchronizationQuery(quotation.getId())
                );
        assertFalse(preview.applyAllowed());
        assertEquals(
                PreviewQuotationOrderSynchronizationUseCase.UNAVAILABLE_LEGACY_UNTRACED,
                preview.unavailableReason()
        );

        OrderDomainException exception = assertThrows(
                OrderDomainException.class,
                () -> applyQuotationChangesToOrderUseCase.execute(
                        new ApplyQuotationChangesToOrderCommand(quotation.getId())
                )
        );
        assertTrue(exception.getMessage().contains("traceability"));
        entityManager.flush();
        entityManager.clear();
        Order unchanged = orderRepository.findById(legacy.getId()).orElseThrow();
        assertNull(unchanged.getItems().getFirst().getQuotationItemId());
        assertEquals(20, unchanged.getItems().getFirst().getQuantity());
    }

    @Test
    void applyUsesCurrentQuotationNotStalePreviewAndPreservesManualItemsPaymentsAndProduction() {
        PreparedOrder prepared = createTracedOrder("Camiseta sync", 10, "100000");
        addOrderItemUseCase.execute(new AddOrderItemCommand(
                prepared.order().getId(),
                "Extra manual",
                2,
                "Hydrotech",
                null,
                "Negro",
                new BigDecimal("15000"),
                null
        ));
        CreateProductionOrderResult production = createProductionOrderFromOrderUseCase.execute(
                new CreateProductionOrderCommand(
                        prepared.order().getId(),
                        ProductionPriority.NORMAL,
                        null,
                        null,
                        "Snapshot 5C"
                )
        );
        registerPaymentUseCase.execute(new RegisterPaymentCommand(
                prepared.order().getId(),
                new BigDecimal("200000.00"),
                QUOTATION_DATE.plusDays(3),
                "Abono"
        ));
        entityManager.flush();
        entityManager.clear();

        PreviewQuotationOrderSynchronizationResult stalePreview =
                previewQuotationOrderSynchronizationUseCase.execute(
                        new PreviewQuotationOrderSynchronizationQuery(prepared.quotationId())
                );
        assertEquals(new BigDecimal("1030000.00"), stalePreview.currentTotal());

        updateQuotationItemUseCase.execute(new UpdateQuotationItemCommand(
                prepared.quotationId(),
                prepared.quotationItemId(),
                "Camiseta sync",
                12,
                "Sudáfrica",
                null,
                "Blanco",
                new BigDecimal("100000"),
                new ProductSpecificationCommand(
                        "Camiseta",
                        "Redondo",
                        "Manga corta sisa",
                        false,
                        false,
                        false,
                        false,
                        null,
                        false,
                        false,
                        false,
                        null,
                        "Post preview"
                )
        ));
        addQuotationItemUseCase.execute(new AddQuotationItemCommand(
                prepared.quotationId(),
                "Pantalón nuevo",
                3,
                "Hydrotech",
                "Negro",
                new BigDecimal("20000"),
                null
        ));
        entityManager.flush();
        entityManager.clear();

        applyQuotationChangesToOrderUseCase.execute(
                new ApplyQuotationChangesToOrderCommand(prepared.quotationId())
        );
        entityManager.flush();
        entityManager.clear();

        Order applied = orderRepository.findById(prepared.order().getId()).orElseThrow();
        Quotation quotation = quotationRepository.findById(prepared.quotationId()).orElseThrow();
        assertEquals(prepared.order().getOrderNumber().getValue(), applied.getOrderNumber().getValue());
        assertEquals(Long.toString(quotation.getQuotationNumber().getValue()), applied.getOrderNumber().getValue());
        assertEquals(3, applied.getItems().size());

        OrderItem traced = applied.getItems().stream()
                .filter(item -> prepared.quotationItemId().equals(item.getQuotationItemId()))
                .findFirst()
                .orElseThrow();
        assertEquals(prepared.orderItemId(), traced.getId());
        assertEquals(12, traced.getQuantity());
        assertEquals("Post preview", traced.getProductSpecification().getItemObservations());

        OrderItem manual = applied.getItems().stream()
                .filter(item -> item.getQuotationItemId() == null)
                .findFirst()
                .orElseThrow();
        assertEquals("Extra manual", manual.getProductName());
        assertEquals(2, manual.getQuantity());

        OrderItem added = applied.getItems().stream()
                .filter(item -> item.getQuotationItemId() != null
                        && !prepared.quotationItemId().equals(item.getQuotationItemId()))
                .findFirst()
                .orElseThrow();
        assertTrue(added.getSizeBreakdowns().isEmpty());
        assertEquals("Pantalón nuevo", added.getProductName());

        List<Payment> payments = paymentRepository.findByOrderId(applied.getId());
        assertEquals(1, payments.size());
        assertEquals(new BigDecimal("200000.00"), payments.getFirst().getAmount().getValue());
        FinancialTransaction transaction = financialTransactionRepository
                .findBySourceTypeAndSourceId(
                        FinancialTransactionSourceType.COMMERCIAL_ORDER,
                        payments.getFirst().getId()
                )
                .orElseThrow();
        assertEquals(new BigDecimal("200000.00"), transaction.getAmount().getValue());

        ProductionOrder snapshot = productionOrderRepository.findById(production.productionOrderId()).orElseThrow();
        assertEquals(2, snapshot.getItems().size());
        assertTrue(snapshot.getItems().stream().anyMatch(item -> item.getQuantity() == 10));
        assertTrue(snapshot.getItems().stream().noneMatch(item -> item.getQuantity() == 12));
        assertEquals(Money.of(new BigDecimal("1290000.00")), applied.getTotal());
    }

    @Test
    void paymentFloorRejectsEntireApplyAndLeavesFinanceAndOrderUnchanged() {
        PreparedOrder prepared = createTracedOrder("Camiseta piso", 10, "100000");
        registerPaymentUseCase.execute(new RegisterPaymentCommand(
                prepared.order().getId(),
                new BigDecimal("900000.00"),
                QUOTATION_DATE.plusDays(3),
                "Pago alto"
        ));
        entityManager.flush();
        List<Payment> paymentsBefore = paymentRepository.findByOrderId(prepared.order().getId());
        UUID paymentId = paymentsBefore.getFirst().getId();
        FinancialTransaction transactionBefore = financialTransactionRepository
                .findBySourceTypeAndSourceId(FinancialTransactionSourceType.COMMERCIAL_ORDER, paymentId)
                .orElseThrow();

        updateQuotationItemUseCase.execute(new UpdateQuotationItemCommand(
                prepared.quotationId(),
                prepared.quotationItemId(),
                "Camiseta piso",
                7,
                "Sudáfrica",
                null,
                "Blanco",
                new BigDecimal("100000"),
                null
        ));
        PreviewQuotationOrderSynchronizationResult preview =
                previewQuotationOrderSynchronizationUseCase.execute(
                        new PreviewQuotationOrderSynchronizationQuery(prepared.quotationId())
                );
        assertTrue(preview.applyAllowed());
        assertTrue(preview.paymentFloorViolation());

        OrderDomainException exception = assertThrows(
                OrderDomainException.class,
                () -> applyQuotationChangesToOrderUseCase.execute(
                        new ApplyQuotationChangesToOrderCommand(prepared.quotationId())
                )
        );
        assertTrue(exception.getMessage().contains("already paid"));
        entityManager.flush();
        entityManager.clear();

        Order unchanged = orderRepository.findById(prepared.order().getId()).orElseThrow();
        assertEquals(10, unchanged.getItems().getFirst().getQuantity());
        assertEquals(Money.of(new BigDecimal("1000000")), unchanged.getTotal());
        assertEquals(1, paymentRepository.findByOrderId(unchanged.getId()).size());
        assertEquals(transactionBefore.getId(), financialTransactionRepository
                .findBySourceTypeAndSourceId(FinancialTransactionSourceType.COMMERCIAL_ORDER, paymentId)
                .orElseThrow()
                .getId());
        Quotation quotation = quotationRepository.findById(prepared.quotationId()).orElseThrow();
        assertEquals(7, quotation.getItems().getFirst().getQuantity());
    }

    @Test
    void sizeConstraintRejectsEntireApply() {
        PreparedOrder prepared = createTracedOrder("Camiseta tallas", 12, "20000");
        replaceOrderItemSizesUseCase.execute(new ReplaceOrderItemSizesCommand(
                prepared.order().getId(),
                prepared.orderItemId(),
                List.of(new SizeBreakdownCommand("S", 6), new SizeBreakdownCommand("M", 6))
        ));
        updateQuotationItemUseCase.execute(new UpdateQuotationItemCommand(
                prepared.quotationId(),
                prepared.quotationItemId(),
                "Camiseta tallas",
                8,
                "Sudáfrica",
                null,
                "Blanco",
                new BigDecimal("20000"),
                null
        ));
        PreviewQuotationOrderSynchronizationResult preview =
                previewQuotationOrderSynchronizationUseCase.execute(
                        new PreviewQuotationOrderSynchronizationQuery(prepared.quotationId())
                );
        assertTrue(preview.sizeConstraintViolation());

        assertThrows(
                OrderDomainException.class,
                () -> applyQuotationChangesToOrderUseCase.execute(
                        new ApplyQuotationChangesToOrderCommand(prepared.quotationId())
                )
        );
        entityManager.flush();
        entityManager.clear();
        Order unchanged = orderRepository.findById(prepared.order().getId()).orElseThrow();
        assertEquals(12, unchanged.getItems().getFirst().getQuantity());
        assertEquals(12, unchanged.getItems().getFirst().getAssignedSizeQuantity());
    }

    @Test
    void orphanRemovalAppearsInPreviewAndApplyRemovesOnlyTracedItem() {
        PreparedOrder prepared = createTracedOrder("Camiseta huérfana", 10, "20000");
        addQuotationItemUseCase.execute(new AddQuotationItemCommand(
                prepared.quotationId(),
                "Segunda",
                4,
                "Hydrotech",
                "Negro",
                new BigDecimal("15000"),
                null
        ));
        applyQuotationChangesToOrderUseCase.execute(
                new ApplyQuotationChangesToOrderCommand(prepared.quotationId())
        );
        entityManager.flush();
        entityManager.clear();

        Quotation withTwo = quotationRepository.findById(prepared.quotationId()).orElseThrow();
        UUID secondItemId = withTwo.getItems().stream()
                .filter(item -> !item.getId().equals(prepared.quotationItemId()))
                .findFirst()
                .orElseThrow()
                .getId();
        removeQuotationItemUseCase.execute(new RemoveQuotationItemCommand(
                prepared.quotationId(),
                secondItemId
        ));
        PreviewQuotationOrderSynchronizationResult preview =
                previewQuotationOrderSynchronizationUseCase.execute(
                        new PreviewQuotationOrderSynchronizationQuery(prepared.quotationId())
                );
        assertEquals(1, preview.orphanRemovals().size());
        assertEquals("Segunda", preview.orphanRemovals().getFirst().productName());

        applyQuotationChangesToOrderUseCase.execute(
                new ApplyQuotationChangesToOrderCommand(prepared.quotationId())
        );
        entityManager.flush();
        entityManager.clear();
        Order applied = orderRepository.findById(prepared.order().getId()).orElseThrow();
        assertEquals(1, applied.getItems().size());
        assertEquals(prepared.quotationItemId(), applied.getItems().getFirst().getQuotationItemId());
    }

    private PreparedOrder createTracedOrder(String productName, int quantity, String unitPrice) {
        UUID sellerId = FixedSellerEmployeeFixture.create(
                createPayrollEmployeeUseCase,
                "Seller-sync-" + UUID.randomUUID().toString().substring(0, 8)
        );
        var customer = createCustomerUseCase.execute(
                new CreateCustomerCommand("Cliente sync " + UUID.randomUUID())
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
        Order order = orderRepository.findById(created.orderId()).orElseThrow();
        Quotation persisted = quotationRepository.findById(quotation.quotationId()).orElseThrow();
        return new PreparedOrder(
                quotation.quotationId(),
                persisted.getItems().getFirst().getId(),
                order.getItems().getFirst().getId(),
                order
        );
    }

    private PreparedQuotation createApprovedQuotationWithoutOrder(String productName, int quantity, String unitPrice) {
        UUID sellerId = FixedSellerEmployeeFixture.create(
                createPayrollEmployeeUseCase,
                "Seller-prev-" + UUID.randomUUID().toString().substring(0, 8)
        );
        var customer = createCustomerUseCase.execute(
                new CreateCustomerCommand("Cliente prev " + UUID.randomUUID())
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
        entityManager.flush();
        entityManager.clear();
        Quotation persisted = quotationRepository.findById(quotation.quotationId()).orElseThrow();
        return new PreparedQuotation(persisted.getId(), persisted.getItems().getFirst().getId());
    }

    private Order persistLegacyOrderForQuotation(Quotation quotation) {
        LocalDate today = LocalDate.of(2098, 5, 10);
        Order created = Order.create(
                OrderNumber.fromQuotationNumber(quotation.getQuotationNumber()),
                quotation.getCustomerId(),
                quotation.getId(),
                today,
                DeliveryCommitment.of(today.plusDays(10)),
                quotation.getSellerId(),
                "Legacy",
                List.of(OrderItem.reconstitute(
                        UUID.randomUUID(),
                        "Camiseta histórica",
                        20,
                        "Hydrotech",
                        "Azul",
                        Money.of(new BigDecimal("45000")),
                        ProductSpecification.empty(),
                        List.of()
                ))
        );
        Order saved = orderRepository.save(created);
        entityManager.flush();
        entityManager.clear();
        return orderRepository.findById(saved.getId()).orElseThrow();
    }

    private record PreparedOrder(UUID quotationId, UUID quotationItemId, UUID orderItemId, Order order) {
    }

    private record PreparedQuotation(UUID quotationId, UUID itemId) {
    }
}
