package com.magyen.platform.commercial.presentation;

import com.magyen.platform.commercial.domain.DeliveryCommitment;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderItem;
import com.magyen.platform.commercial.domain.OrderNumber;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.ProductSpecification;
import com.magyen.platform.finance.application.dto.RegisterPaymentCommand;
import com.magyen.platform.finance.application.usecase.RegisterPaymentUseCase;
import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.MaterialCode;
import com.magyen.platform.production.application.dto.PlanProductionOrderCommand;
import com.magyen.platform.production.application.dto.RegisterProductionMaterialConsumptionCommand;
import com.magyen.platform.production.application.dto.StartProductionOrderCommand;
import com.magyen.platform.production.application.usecase.PlanProductionOrderUseCase;
import com.magyen.platform.production.application.usecase.RegisterProductionMaterialConsumptionUseCase;
import com.magyen.platform.production.application.usecase.StartProductionOrderUseCase;
import com.magyen.platform.production.domain.ProductionOrder;
import com.magyen.platform.production.domain.ProductionOrderRepository;
import com.magyen.platform.production.domain.ProductionPriority;
import com.magyen.platform.shared.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contrato HTTP de GET /api/v1/orders/{orderId}/profitability.
 */
@SpringBootTest
@Transactional
class OrderProfitabilityApiContractTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

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
    private InventoryItemRepository inventoryItemRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void getsOrderProfitabilityWithCompleteStatus() throws Exception {
        Order order = createOrderWithTotal("1000000.00");
        UUID productionOrderId = createInProgressProductionOrder(order.getId());

        registerPaymentUseCase.execute(new RegisterPaymentCommand(
                order.getId(),
                new BigDecimal("300000.00"),
                LocalDate.of(2026, 8, 10),
                "Abono API"
        ));

        InventoryItem fabric = inventoryItemRepository.save(InventoryItem.create(
                MaterialCode.of("APFC-" + UUID.randomUUID().toString().substring(0, 8)),
                "Tela API",
                "FABRIC",
                "METER",
                new BigDecimal("100.0000"),
                null,
                null,
                new BigDecimal("15000.00")
        ));
        registerProductionMaterialConsumptionUseCase.execute(
                new RegisterProductionMaterialConsumptionCommand(
                        productionOrderId,
                        fabric.getId(),
                        new BigDecimal("10.0000"),
                        "METER",
                        null
                )
        );

        mockMvc.perform(
                        get("/api/v1/orders/{orderId}/profitability", order.getId())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(order.getId().toString()))
                .andExpect(jsonPath("$.orderValue").value(1000000.00))
                .andExpect(jsonPath("$.collectedAmount").value(300000.00))
                .andExpect(jsonPath("$.outstandingAmount").value(700000.00))
                .andExpect(jsonPath("$.materialCost").value(150000.00))
                .andExpect(jsonPath("$.laborCost").value(0.00))
                .andExpect(jsonPath("$.plotterMaterialCost").value(0.00))
                .andExpect(jsonPath("$.plotterCostAttributable").value(false))
                .andExpect(jsonPath("$.totalDirectCost").value(150000.00))
                .andExpect(jsonPath("$.directProfit").value(850000.00))
                .andExpect(jsonPath("$.directMarginPercentage").value(85.00))
                .andExpect(jsonPath("$.unvaluedMaterialConsumptionCount").value(0))
                .andExpect(jsonPath("$.profitabilityStatus").value("COMPLETE"));
    }

    @Test
    void getsNoCostDataWhenOrderHasNoProduction() throws Exception {
        Order order = createOrderWithTotal("200000.00");

        mockMvc.perform(
                        get("/api/v1/orders/{orderId}/profitability", order.getId())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(order.getId().toString()))
                .andExpect(jsonPath("$.orderValue").value(200000.00))
                .andExpect(jsonPath("$.materialCost").value(0.00))
                .andExpect(jsonPath("$.laborCost").value(0.00))
                .andExpect(jsonPath("$.plotterCostAttributable").value(false))
                .andExpect(jsonPath("$.directProfit").value(200000.00))
                .andExpect(jsonPath("$.profitabilityStatus").value("NO_COST_DATA"));
    }

    @Test
    void returnsBadRequestWhenOrderDoesNotExist() throws Exception {
        mockMvc.perform(
                        get("/api/v1/orders/{orderId}/profitability", UUID.randomUUID())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    private Order createOrderWithTotal(String unitPrice) {
        LocalDate today = LocalDate.of(2026, 8, 1);
        OrderItem item = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Producto API rentabilidad",
                1,
                "Tela",
                "Negro",
                Money.of(new BigDecimal(unitPrice)),
                ProductSpecification.empty(),
                List.of()
        );

        Order order = Order.create(
                OrderNumber.of("ORD-APIP-" + UUID.randomUUID().toString().substring(0, 8)),
                UUID.randomUUID(),
                UUID.randomUUID(),
                today,
                DeliveryCommitment.of(today.plusDays(7)),
                "Tester",
                "Orden API rentabilidad",
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
                "api profitability"
        ));
        planProductionOrderUseCase.execute(new PlanProductionOrderCommand(
                created.getId(),
                LocalDate.now(),
                LocalDate.now().plusDays(3),
                ProductionPriority.NORMAL
        ));
        startProductionOrderUseCase.execute(new StartProductionOrderCommand(created.getId()));
        return created.getId();
    }
}
