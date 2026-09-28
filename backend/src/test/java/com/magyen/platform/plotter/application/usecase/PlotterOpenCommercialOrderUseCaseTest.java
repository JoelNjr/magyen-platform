package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.commercial.application.dto.AddQuotationItemCommand;
import com.magyen.platform.commercial.application.dto.ApproveQuotationCommand;
import com.magyen.platform.commercial.application.dto.CloseOrderCommand;
import com.magyen.platform.commercial.application.dto.CreateCustomerCommand;
import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateQuotationCommand;
import com.magyen.platform.commercial.application.dto.DeliverOrderCommand;
import com.magyen.platform.commercial.application.dto.MarkOrderReadyForDeliveryCommand;
import com.magyen.platform.commercial.application.dto.StartOrderProductionCommand;
import com.magyen.platform.commercial.application.usecase.AddQuotationItemUseCase;
import com.magyen.platform.commercial.application.usecase.ApproveQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.CloseOrderUseCase;
import com.magyen.platform.commercial.application.usecase.CreateCustomerUseCase;
import com.magyen.platform.commercial.application.usecase.CreateOrderFromQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.CreateQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.DeliverOrderUseCase;
import com.magyen.platform.commercial.application.usecase.MarkOrderReadyForDeliveryUseCase;
import com.magyen.platform.commercial.application.usecase.StartOrderProductionUseCase;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.finance.application.dto.RegisterPaymentCommand;
import com.magyen.platform.finance.application.usecase.CreatePayrollEmployeeUseCase;
import com.magyen.platform.finance.application.usecase.RegisterPaymentUseCase;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemCommand;
import com.magyen.platform.inventory.application.usecase.CreateInventoryItemUseCase;
import com.magyen.platform.plotter.application.dto.CreatePlotterJobCommand;
import com.magyen.platform.plotter.application.port.PlotterCommercialOrderView;
import com.magyen.platform.plotter.domain.PlotterJobType;
import com.magyen.platform.plotter.domain.exception.PlotterDomainException;
import com.magyen.platform.shared.testsupport.FixedSellerEmployeeFixture;
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
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class PlotterOpenCommercialOrderUseCaseTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

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
    private StartOrderProductionUseCase startOrderProductionUseCase;

    @Autowired
    private MarkOrderReadyForDeliveryUseCase markOrderReadyForDeliveryUseCase;

    @Autowired
    private DeliverOrderUseCase deliverOrderUseCase;

    @Autowired
    private CloseOrderUseCase closeOrderUseCase;

    @Autowired
    private RegisterPaymentUseCase registerPaymentUseCase;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CreatePayrollEmployeeUseCase createPayrollEmployeeUseCase;

    @Autowired
    private CreateInventoryItemUseCase createInventoryItemUseCase;

    @Autowired
    private CreatePlotterJobUseCase createPlotterJobUseCase;

    @Autowired
    private GetOpenCommercialOrdersForPlotterUseCase getOpenCommercialOrdersForPlotterUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void listsOnlyOpenOrdersAndRejectsAFinishedOrder() throws Exception {
        UUID customerId = createCustomerUseCase.execute(
                new CreateCustomerCommand("Magyen abierto " + UUID.randomUUID())
        ).customerId();
        UUID openOrderId = createOrder(customerId, "Pedido abierto");
        UUID inProductionOrderId = createOrder(customerId, "Pedido en producción");
        startOrderProductionUseCase.execute(new StartOrderProductionCommand(inProductionOrderId));
        UUID deliveredOrderId = createOrder(customerId, "Pedido entregado");
        deliver(deliveredOrderId);
        UUID closedOrderId = createOrder(customerId, "Pedido cerrado");
        deliver(closedOrderId);
        collectFullPayment(closedOrderId);
        closeOrderUseCase.execute(new CloseOrderCommand(closedOrderId));

        var openOrders = getOpenCommercialOrdersForPlotterUseCase.execute();
        assertTrue(contains(openOrders, openOrderId));
        assertTrue(contains(openOrders, inProductionOrderId));
        assertTrue(openOrders.stream().filter(order -> openOrderId.equals(order.orderId()))
                .allMatch(PlotterCommercialOrderView::openForPlotterJob));
        assertTrue(openOrders.stream().noneMatch(order -> deliveredOrderId.equals(order.orderId())));
        assertTrue(openOrders.stream().noneMatch(order -> closedOrderId.equals(order.orderId())));

        var roll = createInventoryItemUseCase.execute(new CreateInventoryItemCommand(
                "OPN-" + UUID.randomUUID().toString().substring(0, 8),
                "Papel abierto",
                "PAPER",
                "METER",
                new BigDecimal("20.0000"),
                new BigDecimal("1.0000"),
                null,
                new BigDecimal("1000.00"),
                "PAPER",
                true
        ));
        var created = createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                null,
                openOrderId,
                LocalDate.now(),
                roll.inventoryItemId(),
                new BigDecimal("1.0000"),
                new BigDecimal("5000"),
                "interno en orden abierta",
                PlotterJobType.INTERNAL_MAGYEN,
                null
        ));
        assertTrue(openOrderId.equals(created.orderId()));

        PlotterDomainException rejected = assertThrows(PlotterDomainException.class, () ->
                createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                        null,
                        deliveredOrderId,
                        LocalDate.now(),
                        roll.inventoryItemId(),
                        new BigDecimal("1.0000"),
                        new BigDecimal("5000"),
                        "interno en orden entregada",
                        PlotterJobType.INTERNAL_MAGYEN,
                        null
                ))
        );
        assertTrue(rejected.getMessage().contains("open commercial order"));

        mockMvc.perform(get("/api/v1/plotter/open-commercial-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders[*].orderId", hasItem(openOrderId.toString())))
                .andExpect(jsonPath("$.orders[*].orderId", not(hasItem(deliveredOrderId.toString()))))
                .andExpect(jsonPath("$.orders[*].orderId", not(hasItem(closedOrderId.toString()))));

        mockMvc.perform(post("/api/v1/plotter/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId": "%s",
                                  "jobType": "INTERNAL_MAGYEN",
                                  "creationDate": "%s",
                                  "paperInventoryItemId": "%s",
                                  "printedMeters": 1,
                                  "pricePerMeter": 5000
                                }
                                """.formatted(
                                closedOrderId,
                                LocalDate.now(),
                                roll.inventoryItemId()
                        )))
                .andExpect(status().isBadRequest());
    }

    private static boolean contains(java.util.List<PlotterCommercialOrderView> orders, UUID orderId) {
        return orders.stream().anyMatch(order -> orderId.equals(order.orderId()));
    }

    private void deliver(UUID orderId) {
        startOrderProductionUseCase.execute(new StartOrderProductionCommand(orderId));
        markOrderReadyForDeliveryUseCase.execute(new MarkOrderReadyForDeliveryCommand(orderId));
        deliverOrderUseCase.execute(new DeliverOrderCommand(orderId, LocalDate.now()));
    }

    private void collectFullPayment(UUID orderId) {
        var order = orderRepository.findById(orderId).orElseThrow();
        registerPaymentUseCase.execute(new RegisterPaymentCommand(
                orderId,
                order.getTotal().getAmount(),
                LocalDate.now(),
                "cobro que cubre el total"
        ));
    }

    private UUID createOrder(UUID customerId, String description) {
        LocalDate today = LocalDate.now();
        UUID sellerId = FixedSellerEmployeeFixture.create(
                createPayrollEmployeeUseCase,
                "Vendedor abierto " + UUID.randomUUID()
        );
        var quotation = createQuotationUseCase.execute(new CreateQuotationCommand(
                customerId,
                today.plusDays(5),
                sellerId,
                null,
                today
        ));
        addQuotationItemUseCase.execute(new AddQuotationItemCommand(
                quotation.quotationId(),
                "Camiseta abierta",
                1,
                "Sudáfrica",
                "Blanco",
                new BigDecimal("10000"),
                null
        ));
        approveQuotationUseCase.execute(new ApproveQuotationCommand(quotation.quotationId()));
        return createOrderFromQuotationUseCase.execute(new CreateOrderFromQuotationCommand(
                quotation.quotationId(),
                description,
                today,
                today.plusDays(5),
                null
        )).orderId();
    }
}
