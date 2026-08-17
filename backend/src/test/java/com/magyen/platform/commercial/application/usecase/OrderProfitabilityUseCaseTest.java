package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityQuery;
import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityResult;
import com.magyen.platform.commercial.domain.DeliveryCommitment;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderItem;
import com.magyen.platform.commercial.domain.OrderNumber;
import com.magyen.platform.commercial.domain.OrderProfitabilityStatus;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.OrderStatus;
import com.magyen.platform.commercial.domain.PaymentSummary;
import com.magyen.platform.commercial.domain.ProductSpecification;
import com.magyen.platform.finance.application.dto.RegisterPaymentCommand;
import com.magyen.platform.finance.application.usecase.RegisterPaymentUseCase;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.inventory.application.dto.RegisterInventoryPurchaseCommand;
import com.magyen.platform.inventory.application.usecase.RegisterInventoryPurchaseUseCase;
import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.MaterialCode;
import com.magyen.platform.production.application.dto.CancelProductionLaborWorkCommand;
import com.magyen.platform.production.application.dto.CreateProductionOperatorCommand;
import com.magyen.platform.production.application.dto.CreateProductionOperatorResult;
import com.magyen.platform.production.application.dto.PlanProductionOrderCommand;
import com.magyen.platform.production.application.dto.RegisterProductionLaborWorkCommand;
import com.magyen.platform.production.application.dto.RegisterProductionLaborWorkResult;
import com.magyen.platform.production.application.dto.RegisterProductionMaterialConsumptionCommand;
import com.magyen.platform.production.application.dto.StartProductionOrderCommand;
import com.magyen.platform.production.application.usecase.CancelProductionLaborWorkUseCase;
import com.magyen.platform.production.application.usecase.CreateProductionOperatorUseCase;
import com.magyen.platform.production.application.usecase.PlanProductionOrderUseCase;
import com.magyen.platform.production.application.usecase.RegisterProductionLaborWorkUseCase;
import com.magyen.platform.production.application.usecase.RegisterProductionMaterialConsumptionUseCase;
import com.magyen.platform.production.application.usecase.StartProductionOrderUseCase;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import com.magyen.platform.production.domain.ProductionPriority;
import com.magyen.platform.shared.domain.Money;
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

/**
 * Fundamento de rentabilidad directa de Orden (SPR-036 Increment 12).
 * <p>
 * Solo lectura: la consulta no crea FinancialTransaction.
 */
@SpringBootTest
@Transactional
class OrderProfitabilityUseCaseTest {

    @Autowired
    private GetOrderProfitabilityUseCase getOrderProfitabilityUseCase;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RegisterPaymentUseCase registerPaymentUseCase;

    @Autowired
    private ProductionOrderRepository productionOrderRepository;

    @Autowired
    private PlanProductionOrderUseCase planProductionOrderUseCase;

    @Autowired
    private StartProductionOrderUseCase startProductionOrderUseCase;

    @Autowired
    private RegisterProductionMaterialConsumptionUseCase registerProductionMaterialConsumptionUseCase;

    @Autowired
    private RegisterProductionLaborWorkUseCase registerProductionLaborWorkUseCase;

    @Autowired
    private CancelProductionLaborWorkUseCase cancelProductionLaborWorkUseCase;

    @Autowired
    private CreateProductionOperatorUseCase createProductionOperatorUseCase;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    @Autowired
    private RegisterInventoryPurchaseUseCase registerInventoryPurchaseUseCase;

    @Test
    void calculatesCompleteProfitabilityWithPaymentsMaterialAndLabor() {
        Order order = createOrderWithTotal("1000000.00");
        UUID productionOrderId = createInProgressProductionOrder(order.getId());

        registerPaymentUseCase.execute(new RegisterPaymentCommand(
                order.getId(),
                new BigDecimal("400000.00"),
                LocalDate.of(2026, 8, 10),
                "Abono"
        ));
        registerPaymentUseCase.execute(new RegisterPaymentCommand(
                order.getId(),
                new BigDecimal("100000.00"),
                LocalDate.of(2026, 8, 11),
                "Segundo abono"
        ));

        InventoryItem fabric = createFabric("15000.00", "100.0000");
        registerMaterial(productionOrderId, fabric.getId(), "10.0000", "METER");

        CreateProductionOperatorResult operator = createProductionOperator();
        registerLabor(productionOrderId, operator.operatorId(), "100", "800.00");
        // Unpaid PENDING labor must still count toward laborCost
        registerLabor(productionOrderId, operator.operatorId(), "50", "200.00");
        RegisterProductionLaborWorkResult cancelled = registerLabor(
                productionOrderId,
                operator.operatorId(),
                "10",
                "100.00"
        );
        cancelProductionLaborWorkUseCase.execute(
                new CancelProductionLaborWorkCommand(productionOrderId, cancelled.laborWorkId())
        );

        long financeCountBefore = countAllFinancialTransactions();

        GetOrderProfitabilityResult result = getOrderProfitabilityUseCase.execute(
                new GetOrderProfitabilityQuery(order.getId())
        );

        assertEquals(financeCountBefore, countAllFinancialTransactions());
        assertEquals(order.getId(), result.orderId());
        assertEquals(new BigDecimal("1000000.00"), result.orderValue());
        assertEquals(new BigDecimal("500000.00"), result.collectedAmount());
        assertEquals(new BigDecimal("500000.00"), result.outstandingAmount());
        assertEquals(new BigDecimal("150000.00"), result.materialCost());
        // PENDING unpaid (10000) + first labor (80000); cancelled excluded
        assertEquals(new BigDecimal("90000.00"), result.laborCost());
        assertEquals(new BigDecimal("0.00"), result.plotterMaterialCost());
        assertFalse(result.plotterCostAttributable());
        assertEquals(new BigDecimal("240000.00"), result.totalDirectCost());
        assertEquals(new BigDecimal("760000.00"), result.directProfit());
        assertEquals(new BigDecimal("76.00"), result.directMarginPercentage());
        assertEquals(0, result.unvaluedMaterialConsumptionCount());
        assertEquals(OrderProfitabilityStatus.COMPLETE, result.profitabilityStatus());
    }

    @Test
    void changingInventoryUnitCostAfterConsumptionDoesNotChangeMaterialCost() {
        Order order = createOrderWithTotal("500000.00");
        UUID productionOrderId = createInProgressProductionOrder(order.getId());
        InventoryItem fabric = createFabric("15000.00", "100.0000");
        registerMaterial(productionOrderId, fabric.getId(), "10.0000", "METER");

        InventoryItem reloaded = inventoryItemRepository.findById(fabric.getId()).orElseThrow();
        reloaded.updateUnitCost(new BigDecimal("18000.00"));
        inventoryItemRepository.save(reloaded);

        GetOrderProfitabilityResult result = getOrderProfitabilityUseCase.execute(
                new GetOrderProfitabilityQuery(order.getId())
        );

        assertEquals(new BigDecimal("150000.00"), result.materialCost());
        assertEquals(OrderProfitabilityStatus.COMPLETE, result.profitabilityStatus());
    }

    @Test
    void purchaseFinanceExpenseIsNotSubtractedAgainFromOrderProfitability() {
        Order order = createOrderWithTotal("400000.00");
        UUID productionOrderId = createInProgressProductionOrder(order.getId());

        InventoryItem fabric = inventoryItemRepository.save(InventoryItem.create(
                MaterialCode.of("OPFP-" + UUID.randomUUID().toString().substring(0, 8)),
                "Sudáfrica",
                "FABRIC",
                "METER",
                BigDecimal.ZERO,
                null,
                null,
                null,
                com.magyen.platform.inventory.domain.InventoryMaterialType.FABRIC,
                null
        ));

        registerInventoryPurchaseUseCase.execute(new RegisterInventoryPurchaseCommand(
                fabric.getId(),
                UUID.randomUUID(),
                new BigDecimal("100.0000"),
                new BigDecimal("10000.00"),
                LocalDate.of(2026, 8, 16),
                "compra Sudáfrica"
        ));

        registerMaterial(productionOrderId, fabric.getId(), "6.5000", "METER");

        CreateProductionOperatorResult operator = createProductionOperator();
        registerLabor(productionOrderId, operator.operatorId(), "30", "1000.00");

        GetOrderProfitabilityResult result = getOrderProfitabilityUseCase.execute(
                new GetOrderProfitabilityQuery(order.getId())
        );

        assertEquals(new BigDecimal("400000.00"), result.orderValue());
        assertEquals(new BigDecimal("65000.00"), result.materialCost());
        assertEquals(new BigDecimal("30000.00"), result.laborCost());
        assertEquals(new BigDecimal("95000.00"), result.totalDirectCost());
        assertEquals(new BigDecimal("305000.00"), result.directProfit());
        assertEquals(0, result.unvaluedMaterialConsumptionCount());

        long purchaseExpenses = financialTransactionRepository.findAllNewestFirst().stream()
                .filter(transaction -> transaction.getSourceType()
                        == FinancialTransactionSourceType.INVENTORY_PURCHASE)
                .filter(transaction -> transaction.getType() == FinancialTransactionType.EXPENSE)
                .count();
        assertEquals(1, purchaseExpenses);
    }

    @Test
    void unvaluedMaterialYieldsPartiallyUnvaluedStatus() {
        Order order = createOrderWithTotal("500000.00");
        UUID productionOrderId = createInProgressProductionOrder(order.getId());

        InventoryItem valued = createFabric("15000.00", "100.0000");
        InventoryItem unvalued = inventoryItemRepository.save(InventoryItem.create(
                MaterialCode.of("OPUV-" + UUID.randomUUID().toString().substring(0, 8)),
                "Tela sin costo",
                "FABRIC",
                "METER",
                new BigDecimal("50.0000"),
                null
        ));
        registerMaterial(productionOrderId, valued.getId(), "10.0000", "METER");
        registerMaterial(productionOrderId, unvalued.getId(), "5.0000", "METER");

        GetOrderProfitabilityResult result = getOrderProfitabilityUseCase.execute(
                new GetOrderProfitabilityQuery(order.getId())
        );

        assertEquals(new BigDecimal("150000.00"), result.materialCost());
        assertEquals(1, result.unvaluedMaterialConsumptionCount());
        assertEquals(OrderProfitabilityStatus.PARTIALLY_UNVALUED, result.profitabilityStatus());
    }

    @Test
    void noProductionYieldsNoCostDataWithProfitEqualToOrderValue() {
        Order order = createOrderWithTotal("750000.00");

        long financeCountBefore = countAllFinancialTransactions();
        GetOrderProfitabilityResult result = getOrderProfitabilityUseCase.execute(
                new GetOrderProfitabilityQuery(order.getId())
        );

        assertEquals(financeCountBefore, countAllFinancialTransactions());
        assertEquals(new BigDecimal("750000.00"), result.orderValue());
        assertEquals(new BigDecimal("0.00"), result.collectedAmount());
        assertEquals(new BigDecimal("750000.00"), result.outstandingAmount());
        assertEquals(new BigDecimal("0.00"), result.materialCost());
        assertEquals(new BigDecimal("0.00"), result.laborCost());
        assertEquals(new BigDecimal("0.00"), result.plotterMaterialCost());
        assertFalse(result.plotterCostAttributable());
        assertEquals(new BigDecimal("0.00"), result.totalDirectCost());
        assertEquals(new BigDecimal("750000.00"), result.directProfit());
        assertEquals(new BigDecimal("100.00"), result.directMarginPercentage());
        assertEquals(OrderProfitabilityStatus.NO_COST_DATA, result.profitabilityStatus());
    }

    @Test
    void productionWithoutConsumptionsOrLaborIsNoCostData() {
        Order order = createOrderWithTotal("100000.00");
        createInProgressProductionOrder(order.getId());

        GetOrderProfitabilityResult result = getOrderProfitabilityUseCase.execute(
                new GetOrderProfitabilityQuery(order.getId())
        );

        assertEquals(OrderProfitabilityStatus.NO_COST_DATA, result.profitabilityStatus());
        assertEquals(new BigDecimal("0.00"), result.materialCost());
        assertEquals(new BigDecimal("0.00"), result.laborCost());
        assertEquals(new BigDecimal("100000.00"), result.directProfit());
    }

    @Test
    void zeroOrderValueYieldsNullMargin() {
        // Order.create rechaza unitPrice=0; se reconstitue un caso borde de valor cero.
        Order order = createZeroValueOrder();

        GetOrderProfitabilityResult result = getOrderProfitabilityUseCase.execute(
                new GetOrderProfitabilityQuery(order.getId())
        );

        assertEquals(new BigDecimal("0.00"), result.orderValue());
        assertNull(result.directMarginPercentage());
        assertEquals(OrderProfitabilityStatus.NO_COST_DATA, result.profitabilityStatus());
        assertFalse(result.plotterCostAttributable());
    }

    @Test
    void unknownOrderThrowsIllegalArgumentException() {
        UUID missing = UUID.randomUUID();
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> getOrderProfitabilityUseCase.execute(new GetOrderProfitabilityQuery(missing))
        );
        assertTrue(exception.getMessage().contains(missing.toString()));
    }

    private Order createOrderWithTotal(String unitPrice) {
        LocalDate today = LocalDate.of(2026, 8, 1);
        OrderItem item = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Producto rentabilidad",
                1,
                "Tela",
                "Negro",
                Money.of(new BigDecimal(unitPrice)),
                ProductSpecification.empty(),
                List.of()
        );

        Order order = Order.create(
                OrderNumber.of("ORD-PROF-" + UUID.randomUUID().toString().substring(0, 8)),
                UUID.randomUUID(),
                UUID.randomUUID(),
                today,
                DeliveryCommitment.of(today.plusDays(7)),
                UUID.randomUUID(),
                "Orden rentabilidad",
                List.of(item)
        );

        return orderRepository.save(order);
    }

    private Order createZeroValueOrder() {
        LocalDate today = LocalDate.of(2026, 8, 1);
        Money zero = Money.zero();
        OrderItem item = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Producto valor cero",
                1,
                "Tela",
                "Negro",
                zero,
                ProductSpecification.empty(),
                List.of()
        );

        Order order = Order.reconstitute(
                UUID.randomUUID(),
                OrderNumber.of("ORD-ZERO-" + UUID.randomUUID().toString().substring(0, 8)),
                UUID.randomUUID(),
                UUID.randomUUID(),
                today,
                OrderStatus.CONFIRMED,
                DeliveryCommitment.of(today.plusDays(7)),
                PaymentSummary.forConfirmedOrder(zero),
                UUID.randomUUID(),
                "Orden valor cero",
                List.of(item)
        );

        return orderRepository.save(order);
    }

    private UUID createInProgressProductionOrder(UUID orderId) {
        ProductionOrder created = productionOrderRepository.save(ProductionOrder.create(
                orderId,
                LocalDate.now(),
                ProductionPriority.NORMAL,
                null,
                null,
                "profitability production"
        ));
        planProductionOrderUseCase.execute(new PlanProductionOrderCommand(
                created.getId(),
                LocalDate.now(),
                LocalDate.now().plusDays(3),
                ProductionPriority.NORMAL
        ));
        startProductionOrderUseCase.execute(new StartProductionOrderCommand(created.getId(), null));
        return created.getId();
    }

    private InventoryItem createFabric(String unitCost, String stock) {
        return inventoryItemRepository.save(InventoryItem.create(
                MaterialCode.of("OPFC-" + UUID.randomUUID().toString().substring(0, 8)),
                "Tela rentabilidad",
                "FABRIC",
                "METER",
                new BigDecimal(stock),
                null,
                null,
                new BigDecimal(unitCost)
        ));
    }

    private void registerMaterial(
            UUID productionOrderId,
            UUID inventoryItemId,
            String quantity,
            String unit
    ) {
        registerProductionMaterialConsumptionUseCase.execute(
                new RegisterProductionMaterialConsumptionCommand(
                        productionOrderId,
                        inventoryItemId,
                        new BigDecimal(quantity),
                        unit,
                        null
                )
        );
    }

    private RegisterProductionLaborWorkResult registerLabor(
            UUID productionOrderId,
            UUID operatorEmployeeId,
            String quantity,
            String unitRate
    ) {
        return registerProductionLaborWorkUseCase.execute(new RegisterProductionLaborWorkCommand(
                productionOrderId,
                operatorEmployeeId,
                LocalDate.of(2026, 8, 10),
                "Confección",
                new BigDecimal(quantity),
                "UNIT",
                new BigDecimal(unitRate),
                null
        ));
    }

    private CreateProductionOperatorResult createProductionOperator() {
        return createProductionOperatorUseCase.execute(new CreateProductionOperatorCommand(
                "Operario-Prof-" + UUID.randomUUID().toString().substring(0, 8)
        ));
    }

    private long countAllFinancialTransactions() {
        return financialTransactionRepository.findAllNewestFirst().size();
    }
}
