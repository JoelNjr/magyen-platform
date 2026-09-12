package com.magyen.platform.home.application.usecase;

import com.magyen.platform.commercial.domain.DeliveryCommitment;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderItem;
import com.magyen.platform.commercial.domain.OrderNumber;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.OrderStatus;
import com.magyen.platform.commercial.domain.PaymentSummary;
import com.magyen.platform.commercial.domain.ProductSpecification;
import com.magyen.platform.finance.application.dto.RegisterPaymentCommand;
import com.magyen.platform.finance.application.usecase.RegisterPaymentUseCase;
import com.magyen.platform.home.application.dto.GetHomeDashboardQuery;
import com.magyen.platform.home.application.dto.GetHomeDashboardResult;
import com.magyen.platform.home.application.dto.HomeProfitabilitySummary;
import com.magyen.platform.production.application.dto.CompleteProductionOrderCommand;
import com.magyen.platform.production.application.dto.PlanProductionOrderCommand;
import com.magyen.platform.production.application.dto.RegisterProductionAdditionalCostCommand;
import com.magyen.platform.production.application.dto.StartProductionOrderCommand;
import com.magyen.platform.production.application.usecase.CompleteProductionOrderUseCase;
import com.magyen.platform.production.application.usecase.PlanProductionOrderUseCase;
import com.magyen.platform.production.application.usecase.RegisterProductionAdditionalCostUseCase;
import com.magyen.platform.production.application.usecase.StartProductionOrderUseCase;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import com.magyen.platform.production.domain.ProductionPriority;
import com.magyen.platform.production.domain.ProductionStatus;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class HomeDashboardPromisedDeliveryProfitabilityTest {

    private static final LocalDate SEPTEMBER_START = LocalDate.of(2026, 9, 1);
    private static final LocalDate SEPTEMBER_END = LocalDate.of(2026, 9, 30);
    private static final LocalDate AUGUST_START = LocalDate.of(2026, 8, 1);
    private static final LocalDate AUGUST_END = LocalDate.of(2026, 8, 31);
    private static final LocalDate OCTOBER_START = LocalDate.of(2026, 10, 1);
    private static final LocalDate OCTOBER_END = LocalDate.of(2026, 10, 31);

    @Autowired
    private GetHomeDashboardUseCase getHomeDashboardUseCase;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductionOrderRepository productionOrderRepository;

    @Autowired
    private PlanProductionOrderUseCase planProductionOrderUseCase;

    @Autowired
    private StartProductionOrderUseCase startProductionOrderUseCase;

    @Autowired
    private CompleteProductionOrderUseCase completeProductionOrderUseCase;

    @Autowired
    private RegisterProductionAdditionalCostUseCase registerProductionAdditionalCostUseCase;

    @Autowired
    private RegisterPaymentUseCase registerPaymentUseCase;

    @Test
    void promisedDeliveryBoundariesAndConfirmationMonthAreIndependent() {
        HomeProfitabilitySummary septemberBefore = profitability(SEPTEMBER_START, SEPTEMBER_END);

        saveOrder(OrderStatus.CONFIRMED, LocalDate.of(2026, 8, 20), LocalDate.of(2026, 8, 31), "100000.00");
        saveOrder(OrderStatus.CONFIRMED, LocalDate.of(2026, 8, 20), LocalDate.of(2026, 9, 1), "110000.00");
        saveOrder(OrderStatus.CONFIRMED, LocalDate.of(2026, 8, 20), LocalDate.of(2026, 9, 30), "120000.00");
        saveOrder(OrderStatus.CONFIRMED, LocalDate.of(2026, 9, 2), LocalDate.of(2026, 10, 1), "130000.00");
        saveOrder(OrderStatus.CONFIRMED, LocalDate.of(2026, 8, 20), LocalDate.of(2026, 9, 3), "140000.00");
        saveOrder(OrderStatus.CONFIRMED, LocalDate.of(2026, 9, 2), LocalDate.of(2026, 10, 2), "150000.00");

        HomeProfitabilitySummary septemberAfter = profitability(SEPTEMBER_START, SEPTEMBER_END);

        assertEquals(septemberBefore.evaluatedOrderCount() + 3, septemberAfter.evaluatedOrderCount());
        assertEquals(
                septemberBefore.noCostDataOrderCount() + 3,
                septemberAfter.noCostDataOrderCount()
        );
    }

    @Test
    void eligibilityIncludesOpenStatusesAndExcludesClosed() {
        HomeProfitabilitySummary before = profitability(SEPTEMBER_START, SEPTEMBER_END);

        saveOrder(OrderStatus.CONFIRMED, LocalDate.of(2026, 8, 10), LocalDate.of(2026, 9, 5), "160000.00");
        saveOrder(OrderStatus.IN_PRODUCTION, LocalDate.of(2026, 8, 10), LocalDate.of(2026, 9, 6), "170000.00");
        saveOrder(OrderStatus.READY_FOR_DELIVERY, LocalDate.of(2026, 8, 10), LocalDate.of(2026, 9, 7), "180000.00");
        saveOrder(OrderStatus.DELIVERED, LocalDate.of(2026, 8, 10), LocalDate.of(2026, 9, 8), "190000.00");
        saveOrder(OrderStatus.CLOSED, LocalDate.of(2026, 8, 10), LocalDate.of(2026, 9, 9), "200000.00");

        HomeProfitabilitySummary after = profitability(SEPTEMBER_START, SEPTEMBER_END);

        assertEquals(before.evaluatedOrderCount() + 4, after.evaluatedOrderCount());
        assertEquals(before.noCostDataOrderCount() + 4, after.noCostDataOrderCount());
    }

    @Test
    void otherCostAfterCompletedUpdatesLiveProfitabilityWithoutMovingHomePeriod() {
        Order order = saveOrder(
                OrderStatus.CONFIRMED,
                LocalDate.of(2026, 8, 20),
                LocalDate.of(2026, 9, 10),
                "500000.00"
        );
        UUID productionOrderId = completeProduction(order.getId());
        HomeProfitabilitySummary septemberBeforeOther = profitability(SEPTEMBER_START, SEPTEMBER_END);
        HomeProfitabilitySummary octoberBeforeOther = profitability(OCTOBER_START, OCTOBER_END);

        registerProductionAdditionalCostUseCase.execute(new RegisterProductionAdditionalCostCommand(
                productionOrderId,
                "OTHER",
                "Envío pagado después del cierre",
                new BigDecimal("45000.00"),
                LocalDate.of(2026, 9, 12)
        ));

        HomeProfitabilitySummary septemberAfter = profitability(SEPTEMBER_START, SEPTEMBER_END);
        HomeProfitabilitySummary octoberAfter = profitability(OCTOBER_START, OCTOBER_END);

        assertEquals(
                ProductionStatus.COMPLETED,
                productionOrderRepository.findById(productionOrderId).orElseThrow().getStatus()
        );
        Order reloaded = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.CONFIRMED, reloaded.getStatus());
        assertEquals(LocalDate.of(2026, 9, 10), reloaded.getDeliveryCommitment().getPromisedDeliveryDate());

        assertEquals(septemberBeforeOther.evaluatedOrderCount(), septemberAfter.evaluatedOrderCount());
        assertEquals(septemberBeforeOther.completeOrderCount() + 1, septemberAfter.completeOrderCount());
        assertEquals(septemberBeforeOther.noCostDataOrderCount() - 1, septemberAfter.noCostDataOrderCount());
        assertEquals(
                0,
                septemberAfter.totalOrderValue()
                        .compareTo(septemberBeforeOther.totalOrderValue().add(new BigDecimal("500000.00")))
        );
        assertEquals(
                0,
                septemberAfter.totalDirectCost()
                        .compareTo(septemberBeforeOther.totalDirectCost().add(new BigDecimal("45000.00")))
        );
        assertEquals(
                0,
                septemberAfter.totalDirectProfit()
                        .compareTo(septemberBeforeOther.totalDirectProfit().add(new BigDecimal("455000.00")))
        );

        assertEquals(octoberBeforeOther.evaluatedOrderCount(), octoberAfter.evaluatedOrderCount());
        assertEquals(0, octoberAfter.totalOrderValue().compareTo(octoberBeforeOther.totalOrderValue()));
        assertEquals(0, octoberAfter.totalDirectCost().compareTo(octoberBeforeOther.totalDirectCost()));
    }

    @Test
    void octoberPaymentDoesNotMoveSeptemberCommercialProfitability() {
        Order order = saveOrder(
                OrderStatus.CONFIRMED,
                LocalDate.of(2026, 8, 20),
                LocalDate.of(2026, 9, 10),
                "500000.00"
        );
        HomeProfitabilitySummary septemberBeforePayment = profitability(SEPTEMBER_START, SEPTEMBER_END);

        registerPaymentUseCase.execute(new RegisterPaymentCommand(
                order.getId(),
                new BigDecimal("200000.00"),
                LocalDate.of(2026, 10, 5),
                "Abono octubre"
        ));

        HomeProfitabilitySummary septemberAfter = profitability(SEPTEMBER_START, SEPTEMBER_END);
        GetHomeDashboardResult october = dashboard(OCTOBER_START, OCTOBER_END);

        assertEquals(septemberBeforePayment.evaluatedOrderCount(), septemberAfter.evaluatedOrderCount());
        assertEquals(septemberBeforePayment.noCostDataOrderCount(), septemberAfter.noCostDataOrderCount());
        assertEquals(septemberBeforePayment.totalOrderValue(), septemberAfter.totalOrderValue());
        assertTrue(october.financialSummary().income().compareTo(new BigDecimal("200000.00")) >= 0);
        assertEquals(
                profitability(OCTOBER_START, OCTOBER_END).evaluatedOrderCount(),
                october.profitabilitySummary().evaluatedOrderCount()
        );
        assertTrue(
                october.profitabilitySummary().evaluatedOrderCount() <= septemberAfter.evaluatedOrderCount()
        );
    }

    @Test
    void augustAndSeptemberDoNotContaminateEachOtherAndDoNotMutateOrders() {
        HomeProfitabilitySummary augustBefore = profitability(AUGUST_START, AUGUST_END);
        HomeProfitabilitySummary septemberBefore = profitability(SEPTEMBER_START, SEPTEMBER_END);

        Order augustOrder = saveOrder(
                OrderStatus.CONFIRMED,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 15),
                "210000.00"
        );
        Order septemberOrder = saveOrder(
                OrderStatus.CONFIRMED,
                LocalDate.of(2026, 8, 20),
                LocalDate.of(2026, 9, 12),
                "220000.00"
        );

        HomeProfitabilitySummary august = profitability(AUGUST_START, AUGUST_END);
        HomeProfitabilitySummary september = profitability(SEPTEMBER_START, SEPTEMBER_END);

        assertEquals(augustBefore.evaluatedOrderCount() + 1, august.evaluatedOrderCount());
        assertEquals(septemberBefore.evaluatedOrderCount() + 1, september.evaluatedOrderCount());
        assertEquals(augustBefore.noCostDataOrderCount() + 1, august.noCostDataOrderCount());
        assertEquals(septemberBefore.noCostDataOrderCount() + 1, september.noCostDataOrderCount());

        Order reloadedAugust = orderRepository.findById(augustOrder.getId()).orElseThrow();
        Order reloadedSeptember = orderRepository.findById(septemberOrder.getId()).orElseThrow();
        assertEquals(OrderStatus.CONFIRMED, reloadedAugust.getStatus());
        assertEquals(LocalDate.of(2026, 8, 15), reloadedAugust.getDeliveryCommitment().getPromisedDeliveryDate());
        assertEquals(LocalDate.of(2026, 8, 1), reloadedAugust.getConfirmationDate());
        assertEquals(OrderStatus.CONFIRMED, reloadedSeptember.getStatus());
        assertEquals(LocalDate.of(2026, 9, 12), reloadedSeptember.getDeliveryCommitment().getPromisedDeliveryDate());
        assertEquals(LocalDate.of(2026, 8, 20), reloadedSeptember.getConfirmationDate());
        assertEquals(new BigDecimal("210000.00"), reloadedAugust.getTotal().getAmount());
        assertEquals(new BigDecimal("220000.00"), reloadedSeptember.getTotal().getAmount());
    }

    private HomeProfitabilitySummary profitability(LocalDate fromDate, LocalDate toDate) {
        return dashboard(fromDate, toDate).profitabilitySummary();
    }

    private GetHomeDashboardResult dashboard(LocalDate fromDate, LocalDate toDate) {
        return getHomeDashboardUseCase.execute(new GetHomeDashboardQuery(fromDate, toDate));
    }

    private UUID completeProduction(UUID orderId) {
        ProductionOrder created = productionOrderRepository.save(ProductionOrder.create(
                orderId,
                LocalDate.of(2026, 9, 1),
                ProductionPriority.NORMAL,
                null,
                null,
                "home promised delivery"
        ));
        planProductionOrderUseCase.execute(new PlanProductionOrderCommand(
                created.getId(),
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 8),
                ProductionPriority.NORMAL
        ));
        startProductionOrderUseCase.execute(new StartProductionOrderCommand(
                created.getId(),
                LocalDate.of(2026, 9, 2)
        ));
        completeProductionOrderUseCase.execute(new CompleteProductionOrderCommand(
                created.getId(),
                LocalDate.of(2026, 9, 10)
        ));
        return created.getId();
    }

    private Order saveOrder(
            OrderStatus status,
            LocalDate confirmationDate,
            LocalDate promisedDeliveryDate,
            String unitPrice
    ) {
        OrderItem item = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Camiseta Home período",
                1,
                "Algodón",
                "Blanco",
                Money.of(new BigDecimal(unitPrice)),
                ProductSpecification.empty(),
                List.of()
        );
        Money total = item.getSubtotal();
        PaymentSummary payment = status == OrderStatus.CLOSED
                ? PaymentSummary.of(true, true, total)
                : PaymentSummary.forConfirmedOrder(total);
        return orderRepository.save(Order.reconstitute(
                UUID.randomUUID(),
                OrderNumber.of("ORD-HPD-" + UUID.randomUUID().toString().substring(0, 8)),
                UUID.randomUUID(),
                UUID.randomUUID(),
                confirmationDate,
                status,
                DeliveryCommitment.of(promisedDeliveryDate),
                payment,
                UUID.randomUUID(),
                null,
                "Home promised delivery",
                List.of(item)
        ));
    }
}
