package com.magyen.platform.plotter.application.usecase;

import com.magyen.platform.commercial.application.dto.AddQuotationItemCommand;
import com.magyen.platform.commercial.application.dto.ApproveQuotationCommand;
import com.magyen.platform.commercial.application.dto.CloseOrderCommand;
import com.magyen.platform.commercial.application.dto.CreateCustomerCommand;
import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateQuotationCommand;
import com.magyen.platform.commercial.application.dto.DeliverOrderCommand;
import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityListQuery;
import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityQuery;
import com.magyen.platform.commercial.application.dto.MarkOrderReadyForDeliveryCommand;
import com.magyen.platform.commercial.application.dto.StartOrderProductionCommand;
import com.magyen.platform.commercial.application.usecase.AddQuotationItemUseCase;
import com.magyen.platform.commercial.application.usecase.ApproveQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.CloseOrderUseCase;
import com.magyen.platform.commercial.application.usecase.CreateCustomerUseCase;
import com.magyen.platform.commercial.application.usecase.CreateOrderFromQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.CreateQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.DeliverOrderUseCase;
import com.magyen.platform.commercial.application.usecase.GetOrderProfitabilityListUseCase;
import com.magyen.platform.commercial.application.usecase.GetOrderProfitabilityUseCase;
import com.magyen.platform.commercial.application.usecase.MarkOrderReadyForDeliveryUseCase;
import com.magyen.platform.commercial.application.usecase.StartOrderProductionUseCase;
import com.magyen.platform.commercial.domain.CustomerCategory;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.OrderStatus;
import com.magyen.platform.finance.application.dto.RegisterPaymentCommand;
import com.magyen.platform.finance.application.usecase.CreatePayrollEmployeeUseCase;
import com.magyen.platform.finance.application.usecase.RegisterPaymentUseCase;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionSourceType;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemCommand;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemResult;
import com.magyen.platform.inventory.application.usecase.CreateInventoryItemUseCase;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.InventoryMovementRepository;
import com.magyen.platform.inventory.domain.InventoryMovementSourceType;
import com.magyen.platform.inventory.domain.InventoryMovementType;
import com.magyen.platform.plotter.application.dto.CreatePlotterJobCommand;
import com.magyen.platform.plotter.application.dto.CreatePlotterJobResult;
import com.magyen.platform.plotter.application.dto.GetHistoricalCommercialOrdersForPlotterQuery;
import com.magyen.platform.plotter.application.dto.GetPlotterProfitabilityQuery;
import com.magyen.platform.plotter.domain.PlotterJobRepository;
import com.magyen.platform.plotter.domain.PlotterJobType;
import com.magyen.platform.plotter.domain.PlotterProfitabilityScope;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class PlotterHistoricalRegistrationUseCaseTest {

    private static final LocalDate SEPTEMBER_CONFIRMATION = LocalDate.of(2026, 9, 5);
    private static final LocalDate SEPTEMBER_DELIVERY = LocalDate.of(2026, 9, 20);
    private static final LocalDate FIRST_WORK_DATE = LocalDate.of(2026, 9, 12);
    private static final LocalDate SECOND_WORK_DATE = LocalDate.of(2026, 9, 18);

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
    private CreatePayrollEmployeeUseCase createPayrollEmployeeUseCase;

    @Autowired
    private CreateInventoryItemUseCase createInventoryItemUseCase;

    @Autowired
    private CreatePlotterJobUseCase createPlotterJobUseCase;

    @Autowired
    private GetOpenCommercialOrdersForPlotterUseCase getOpenCommercialOrdersForPlotterUseCase;

    @Autowired
    private GetHistoricalCommercialOrdersForPlotterUseCase getHistoricalCommercialOrdersForPlotterUseCase;

    @Autowired
    private GetOrderProfitabilityUseCase getOrderProfitabilityUseCase;

    @Autowired
    private GetOrderProfitabilityListUseCase getOrderProfitabilityListUseCase;

    @Autowired
    private GetPlotterProfitabilityUseCase getPlotterProfitabilityUseCase;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PlotterJobRepository plotterJobRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void normalSelectorStaysOpenAndHistoricalSelectorFindsFinishedOrdersByConfirmationMonth() throws Exception {
        UUID customerId = createCustomer("Cliente histórico");
        UUID openOrderId = createOrder(customerId, "Abierto de septiembre", SEPTEMBER_CONFIRMATION, SEPTEMBER_DELIVERY);
        UUID deliveredOrderId = deliverOrder(
                createOrder(customerId, "Entregado de septiembre", SEPTEMBER_CONFIRMATION, SEPTEMBER_DELIVERY)
        );
        UUID closedOrderId = closeOrder(
                createOrder(customerId, "Cerrado de septiembre", SEPTEMBER_CONFIRMATION, SEPTEMBER_DELIVERY)
        );
        UUID augustClosedOrderId = closeOrder(createOrder(
                customerId,
                "Cerrado de agosto",
                LocalDate.of(2026, 8, 4),
                LocalDate.of(2026, 8, 18)
        ));

        var openOrders = getOpenCommercialOrdersForPlotterUseCase.execute();
        assertTrue(openOrders.stream().anyMatch(order -> openOrderId.equals(order.orderId())));
        assertTrue(openOrders.stream().noneMatch(order -> deliveredOrderId.equals(order.orderId())));
        assertTrue(openOrders.stream().noneMatch(order -> closedOrderId.equals(order.orderId())));

        var september = getHistoricalCommercialOrdersForPlotterUseCase.execute(
                new GetHistoricalCommercialOrdersForPlotterQuery(2026, 9)
        );
        assertTrue(september.stream().anyMatch(order ->
                deliveredOrderId.equals(order.orderId())
                        && "DELIVERED".equals(order.status())
                        && customerId.equals(order.customerId())
                        && SEPTEMBER_CONFIRMATION.equals(order.confirmationDate())
        ));
        assertTrue(september.stream().anyMatch(order ->
                closedOrderId.equals(order.orderId()) && "CLOSED".equals(order.status())
        ));
        assertTrue(september.stream().noneMatch(order -> openOrderId.equals(order.orderId())));
        assertTrue(september.stream().noneMatch(order -> augustClosedOrderId.equals(order.orderId())));

        var august = getHistoricalCommercialOrdersForPlotterUseCase.execute(
                new GetHistoricalCommercialOrdersForPlotterQuery(2026, 8)
        );
        assertTrue(august.stream().anyMatch(order -> augustClosedOrderId.equals(order.orderId())));
        assertTrue(august.stream().noneMatch(order -> closedOrderId.equals(order.orderId())));

        mockMvc.perform(get("/api/v1/plotter/open-commercial-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders[*].orderId", hasItem(openOrderId.toString())))
                .andExpect(jsonPath("$.orders[*].orderId", not(hasItem(closedOrderId.toString()))));

        mockMvc.perform(get("/api/v1/plotter/historical-commercial-orders")
                        .param("year", "2026")
                        .param("month", "9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders[*].orderId", hasItem(closedOrderId.toString())))
                .andExpect(jsonPath("$.orders[*].orderId", hasItem(deliveredOrderId.toString())))
                .andExpect(jsonPath("$.orders[*].status", hasItem("CLOSED")))
                .andExpect(jsonPath("$.orders[*].orderId", not(hasItem(openOrderId.toString()))))
                .andExpect(jsonPath("$.orders[*].orderId", not(hasItem(augustClosedOrderId.toString()))));

        assertThrows(PlotterDomainException.class, () ->
                getHistoricalCommercialOrdersForPlotterUseCase.execute(
                        new GetHistoricalCommercialOrdersForPlotterQuery(2026, 13)
                )
        );
    }

    @Test
    void historicalJobsStayOnTheClosedOrderAndCountTheWorkDateOnce() {
        UUID customerId = createCustomer("Cliente cerrado plotter");
        UUID closedOrderId = closeOrder(
                createOrder(customerId, "Pedido cerrado con plotter", SEPTEMBER_CONFIRMATION, SEPTEMBER_DELIVERY)
        );
        Order before = orderRepository.findById(closedOrderId).orElseThrow();
        CreateInventoryItemResult roll = createPaperRoll("20.0000", "1000.00");
        UUID firstJobId = UUID.randomUUID();

        CreatePlotterJobResult first = createPlotterJobUseCase.execute(historicalCommand(
                closedOrderId,
                roll.inventoryItemId(),
                FIRST_WORK_DATE,
                "2.0000",
                "5000",
                firstJobId,
                "primer trabajo atrasado"
        ));
        CreatePlotterJobResult second = createPlotterJobUseCase.execute(historicalCommand(
                closedOrderId,
                roll.inventoryItemId(),
                SECOND_WORK_DATE,
                "3.0000",
                "5000",
                UUID.randomUUID(),
                "segundo trabajo atrasado"
        ));
        createPlotterJobUseCase.execute(historicalCommand(
                closedOrderId,
                roll.inventoryItemId(),
                FIRST_WORK_DATE,
                "2.0000",
                "5000",
                firstJobId,
                "primer trabajo atrasado"
        ));

        Order after = orderRepository.findById(closedOrderId).orElseThrow();
        assertEquals(OrderStatus.CLOSED, after.getStatus());
        assertEquals(before.getTotal().getAmount(), after.getTotal().getAmount());
        assertEquals(before.getConfirmationDate(), after.getConfirmationDate());
        assertEquals(before.getActualDeliveryDate(), after.getActualDeliveryDate());
        assertEquals(0, before.getPaymentSummary().getRemainingBalance().getAmount()
                .compareTo(after.getPaymentSummary().getRemainingBalance().getAmount()));
        assertTrue(after.getPaymentSummary().isFinalPaymentAcknowledged());

        assertEquals(customerId, first.customerId());
        assertEquals(closedOrderId, first.orderId());
        assertEquals(before.getOrderNumber().getValue(), first.orderNumber());
        assertEquals(FIRST_WORK_DATE, first.creationDate());
        assertEquals(new BigDecimal("10000.00"), first.totalAmount());
        assertEquals(closedOrderId, second.orderId());
        assertEquals(SECOND_WORK_DATE, second.creationDate());
        assertEquals(new BigDecimal("15000.00"), second.totalAmount());
        assertEquals(2, plotterJobRepository.findByOrderId(closedOrderId).size());

        assertEquals(0, new BigDecimal("15.0000").compareTo(
                inventoryItemRepository.findById(roll.inventoryItemId()).orElseThrow().getStock()
        ));
        assertEquals(1, countPlotterOuts(first.plotterJobId()));
        assertEquals(1, countPlotterOuts(second.plotterJobId()));
        assertOutQuantity(first.plotterJobId(), "2.0000");
        assertOutQuantity(second.plotterJobId(), "3.0000");

        assertSingleInternalLedger(first.plotterJobId(), new BigDecimal("10000.00"), FIRST_WORK_DATE);
        assertSingleInternalLedger(second.plotterJobId(), new BigDecimal("15000.00"), SECOND_WORK_DATE);
        assertTrue(financialTransactionRepository.findByOrderId(closedOrderId).stream()
                .noneMatch(transaction -> transaction.getSourceType() == FinancialTransactionSourceType.PLOTTER_INTERNAL_EXPENSE
                        || transaction.getSourceType() == FinancialTransactionSourceType.PLOTTER_INTERNAL_INCOME));

        var profitability = getOrderProfitabilityUseCase.execute(new GetOrderProfitabilityQuery(closedOrderId));
        assertEquals(before.getTotal().getAmount(), profitability.orderValue());
        assertEquals(new BigDecimal("5000.00"), profitability.plotterMaterialCost());
        assertEquals(new BigDecimal("25000.00"), profitability.internalPlotterServiceCost());
        assertEquals(new BigDecimal("25000.00"), profitability.totalDirectCost());
        assertEquals(new BigDecimal("0.00"), profitability.otherDirectCost());

        var septemberPlotter = getPlotterProfitabilityUseCase.execute(new GetPlotterProfitabilityQuery(
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30),
                PlotterProfitabilityScope.INTERNAL
        ));
        assertEquals(FIRST_WORK_DATE, septemberPlotter.internalOrders().stream()
                .filter(item -> first.plotterJobId().equals(item.plotterJobId()))
                .findFirst()
                .orElseThrow()
                .jobDate());
        assertTrue(septemberPlotter.internalOrders().stream()
                .anyMatch(item -> second.plotterJobId().equals(item.plotterJobId())
                        && SECOND_WORK_DATE.equals(item.jobDate())
                        && closedOrderId.equals(item.orderId())));
        var octoberPlotter = getPlotterProfitabilityUseCase.execute(new GetPlotterProfitabilityQuery(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31),
                PlotterProfitabilityScope.INTERNAL
        ));
        assertTrue(octoberPlotter.internalOrders().stream().noneMatch(item ->
                first.plotterJobId().equals(item.plotterJobId())
                        || second.plotterJobId().equals(item.plotterJobId())
        ));

        var septemberOrders = getOrderProfitabilityListUseCase.execute(new GetOrderProfitabilityListQuery(
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30)
        ));
        assertTrue(septemberOrders.orders().stream().noneMatch(order -> closedOrderId.equals(order.orderId())));
    }

    @Test
    void deliveredHistoricalJobRecalculatesOrderProfitabilityWithoutReopeningIt() {
        UUID customerId = createCustomer("Cliente entregado plotter");
        UUID deliveredOrderId = deliverOrder(
                createOrder(customerId, "Entregado con plotter", SEPTEMBER_CONFIRMATION, SEPTEMBER_DELIVERY)
        );
        CreateInventoryItemResult roll = createPaperRoll("10.0000", "1000.00");

        CreatePlotterJobResult job = createPlotterJobUseCase.execute(historicalCommand(
                deliveredOrderId,
                roll.inventoryItemId(),
                FIRST_WORK_DATE,
                "4.0000",
                "5000",
                null,
                "trabajo sobre pedido entregado"
        ));

        Order order = orderRepository.findById(deliveredOrderId).orElseThrow();
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
        assertEquals(SEPTEMBER_DELIVERY, order.getActualDeliveryDate());
        assertEquals(customerId, job.customerId());

        var profitability = getOrderProfitabilityUseCase.execute(new GetOrderProfitabilityQuery(deliveredOrderId));
        assertEquals(new BigDecimal("20000.00"), profitability.internalPlotterServiceCost());
        assertEquals(new BigDecimal("20000.00"), profitability.totalDirectCost());

        var septemberOrders = getOrderProfitabilityListUseCase.execute(new GetOrderProfitabilityListQuery(
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30)
        ));
        assertTrue(septemberOrders.orders().stream().anyMatch(item ->
                deliveredOrderId.equals(item.orderId())
                        && item.internalPlotterServiceCost().compareTo(new BigDecimal("20000.00")) == 0
        ));
    }

    @Test
    void historicalFlagDoesNotBypassOrdinaryRestrictionsAndExistingModesStillRegister() throws Exception {
        UUID customerId = createCustomer("Cliente restricciones");
        UUID openOrderId = createOrder(customerId, "Sigue abierto", SEPTEMBER_CONFIRMATION, SEPTEMBER_DELIVERY);
        UUID closedOrderId = closeOrder(
                createOrder(customerId, "Sigue cerrado", SEPTEMBER_CONFIRMATION, SEPTEMBER_DELIVERY)
        );
        CreateInventoryItemResult roll = createPaperRoll("30.0000", "1000.00");

        PlotterDomainException missingDate = assertThrows(PlotterDomainException.class, () ->
                createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                        null,
                        closedOrderId,
                        null,
                        roll.inventoryItemId(),
                        new BigDecimal("1.0000"),
                        new BigDecimal("5000"),
                        null,
                        PlotterJobType.INTERNAL_MAGYEN,
                        null,
                        true
                ))
        );
        assertTrue(missingDate.getMessage().contains("actual work date"));
        assertEquals(0, plotterJobRepository.findByOrderId(closedOrderId).size());

        PlotterDomainException stillOpen = assertThrows(PlotterDomainException.class, () ->
                createPlotterJobUseCase.execute(historicalCommand(
                        openOrderId,
                        roll.inventoryItemId(),
                        FIRST_WORK_DATE,
                        "1.0000",
                        "5000",
                        null,
                        "no es histórico"
                ))
        );
        assertTrue(stillOpen.getMessage().contains("delivered or closed"));

        PlotterDomainException ordinaryClosed = assertThrows(PlotterDomainException.class, () ->
                createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                        null,
                        closedOrderId,
                        FIRST_WORK_DATE,
                        roll.inventoryItemId(),
                        new BigDecimal("1.0000"),
                        new BigDecimal("5000"),
                        null,
                        PlotterJobType.INTERNAL_MAGYEN,
                        null
                ))
        );
        assertTrue(ordinaryClosed.getMessage().contains("open commercial order"));

        PlotterDomainException afterDelivery = assertThrows(PlotterDomainException.class, () ->
                createPlotterJobUseCase.execute(historicalCommand(
                        closedOrderId,
                        roll.inventoryItemId(),
                        LocalDate.of(2026, 10, 14),
                        "1.0000",
                        "5000",
                        null,
                        "fecha posterior a la entrega"
                ))
        );
        assertTrue(afterDelivery.getMessage().contains("after delivery date"));
        assertEquals(OrderStatus.CLOSED, orderRepository.findById(closedOrderId).orElseThrow().getStatus());
        assertEquals(OrderStatus.CONFIRMED, orderRepository.findById(openOrderId).orElseThrow().getStatus());

        UUID externalCustomerId = createCustomerUseCase.execute(new CreateCustomerCommand(
                "Externo " + UUID.randomUUID(),
                CustomerCategory.PLOTTER
        )).customerId();
        PlotterDomainException historicalExternal = assertThrows(PlotterDomainException.class, () ->
                createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                        externalCustomerId,
                        null,
                        FIRST_WORK_DATE,
                        roll.inventoryItemId(),
                        new BigDecimal("1.0000"),
                        new BigDecimal("5000"),
                        null,
                        PlotterJobType.EXTERNAL,
                        null,
                        true
                ))
        );
        assertTrue(historicalExternal.getMessage().contains("internal Magyen"));

        PlotterDomainException historicalWaste = assertThrows(PlotterDomainException.class, () ->
                createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                        null,
                        null,
                        FIRST_WORK_DATE,
                        roll.inventoryItemId(),
                        new BigDecimal("1.0000"),
                        null,
                        "merma",
                        PlotterJobType.WASTE,
                        null,
                        true
                ))
        );
        assertTrue(historicalWaste.getMessage().contains("internal Magyen"));

        var external = createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                externalCustomerId,
                null,
                FIRST_WORK_DATE,
                roll.inventoryItemId(),
                new BigDecimal("1.0000"),
                new BigDecimal("5000"),
                "venta externa",
                PlotterJobType.EXTERNAL,
                null
        ));
        var waste = createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                null,
                null,
                FIRST_WORK_DATE,
                roll.inventoryItemId(),
                new BigDecimal("1.0000"),
                null,
                "merma operativa",
                PlotterJobType.WASTE,
                null
        ));
        assertEquals(PlotterJobType.EXTERNAL, external.jobType());
        assertNull(external.orderId());
        assertEquals(PlotterJobType.WASTE, waste.jobType());
        assertNull(waste.orderId());
        assertEquals(1, countPlotterOuts(external.plotterJobId()));
        assertEquals(1, countPlotterOuts(waste.plotterJobId()));
        assertEquals(0, countSource(FinancialTransactionSourceType.PLOTTER_INTERNAL_EXPENSE, waste.plotterJobId()));

        var ordinaryInternal = createPlotterJobUseCase.execute(new CreatePlotterJobCommand(
                null,
                openOrderId,
                FIRST_WORK_DATE,
                roll.inventoryItemId(),
                new BigDecimal("1.0000"),
                new BigDecimal("5000"),
                "operación diaria",
                PlotterJobType.INTERNAL_MAGYEN,
                null
        ));
        assertEquals(openOrderId, ordinaryInternal.orderId());
        assertEquals(OrderStatus.CONFIRMED, orderRepository.findById(openOrderId).orElseThrow().getStatus());

        mockMvc.perform(post("/api/v1/plotter/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "jobType": "INTERNAL_MAGYEN",
                                  "historicalRegistration": true,
                                  "orderId": "%s",
                                  "creationDate": "2026-09-12",
                                  "paperInventoryItemId": "%s",
                                  "printedMeters": 1,
                                  "pricePerMeter": 5000
                                }
                                """.formatted(closedOrderId, roll.inventoryItemId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(closedOrderId.toString()))
                .andExpect(jsonPath("$.creationDate").value("2026-09-12"));
        assertEquals(OrderStatus.CLOSED, orderRepository.findById(closedOrderId).orElseThrow().getStatus());
    }

    private CreatePlotterJobCommand historicalCommand(
            UUID orderId,
            UUID paperRollId,
            LocalDate workDate,
            String meters,
            String pricePerMeter,
            UUID plotterJobId,
            String observations
    ) {
        return new CreatePlotterJobCommand(
                null,
                orderId,
                workDate,
                paperRollId,
                new BigDecimal(meters),
                new BigDecimal(pricePerMeter),
                observations,
                PlotterJobType.INTERNAL_MAGYEN,
                plotterJobId,
                true
        );
    }

    private void assertSingleInternalLedger(UUID plotterJobId, BigDecimal amount, LocalDate workDate) {
        var expense = financialTransactionRepository.findBySourceTypeAndSourceId(
                FinancialTransactionSourceType.PLOTTER_INTERNAL_EXPENSE,
                plotterJobId
        ).orElseThrow();
        var income = financialTransactionRepository.findBySourceTypeAndSourceId(
                FinancialTransactionSourceType.PLOTTER_INTERNAL_INCOME,
                plotterJobId
        ).orElseThrow();
        assertEquals(amount, expense.getAmount().getValue());
        assertEquals(amount, income.getAmount().getValue());
        assertEquals(workDate, expense.getTransactionDate());
        assertEquals(workDate, income.getTransactionDate());
        assertNull(expense.getOrderId());
        assertNull(income.getOrderId());
        assertEquals(1, countSource(FinancialTransactionSourceType.PLOTTER_INTERNAL_EXPENSE, plotterJobId));
        assertEquals(1, countSource(FinancialTransactionSourceType.PLOTTER_INTERNAL_INCOME, plotterJobId));
    }

    private void assertOutQuantity(UUID plotterJobId, String meters) {
        var movement = inventoryMovementRepository
                .findBySourceTypeAndSourceId(InventoryMovementSourceType.PLOTTER, plotterJobId)
                .orElseThrow();
        assertEquals(InventoryMovementType.OUT, movement.getMovementType());
        assertEquals(0, new BigDecimal(meters).compareTo(movement.getQuantity()));
    }

    private int countPlotterOuts(UUID plotterJobId) {
        return inventoryMovementRepository
                .findBySourceTypeAndSourceId(InventoryMovementSourceType.PLOTTER, plotterJobId)
                .isPresent() ? 1 : 0;
    }

    private long countSource(FinancialTransactionSourceType sourceType, UUID sourceId) {
        return financialTransactionRepository.findAllNewestFirst().stream()
                .filter(transaction -> transaction.getSourceType() == sourceType)
                .filter(transaction -> sourceId.equals(transaction.getSourceId()))
                .count();
    }

    private CreateInventoryItemResult createPaperRoll(String stock, String unitCost) {
        return createInventoryItemUseCase.execute(new CreateInventoryItemCommand(
                "HST-" + UUID.randomUUID().toString().substring(0, 8),
                "Papel histórico",
                "PAPER",
                "METER",
                new BigDecimal(stock),
                new BigDecimal("1.0000"),
                null,
                new BigDecimal(unitCost),
                "PAPER",
                true
        ));
    }

    private UUID createCustomer(String name) {
        return createCustomerUseCase.execute(new CreateCustomerCommand(name + " " + UUID.randomUUID())).customerId();
    }

    private UUID deliverOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        startOrderProductionUseCase.execute(new StartOrderProductionCommand(orderId));
        markOrderReadyForDeliveryUseCase.execute(new MarkOrderReadyForDeliveryCommand(orderId));
        deliverOrderUseCase.execute(new DeliverOrderCommand(
                orderId,
                order.getDeliveryCommitment().getPromisedDeliveryDate()
        ));
        return orderId;
    }

    private UUID closeOrder(UUID orderId) {
        deliverOrder(orderId);
        Order order = orderRepository.findById(orderId).orElseThrow();
        registerPaymentUseCase.execute(new RegisterPaymentCommand(
                orderId,
                order.getTotal().getAmount(),
                order.getActualDeliveryDate(),
                "cobro del pedido histórico"
        ));
        closeOrderUseCase.execute(new CloseOrderCommand(orderId));
        return orderId;
    }

    private UUID createOrder(
            UUID customerId,
            String description,
            LocalDate confirmationDate,
            LocalDate promisedDeliveryDate
    ) {
        UUID sellerId = FixedSellerEmployeeFixture.create(
                createPayrollEmployeeUseCase,
                "Vendedor histórico " + UUID.randomUUID()
        );
        var quotation = createQuotationUseCase.execute(new CreateQuotationCommand(
                customerId,
                promisedDeliveryDate,
                sellerId,
                null,
                confirmationDate
        ));
        addQuotationItemUseCase.execute(new AddQuotationItemCommand(
                quotation.quotationId(),
                "Bandera histórica",
                2,
                "Sudáfrica",
                "Blanco",
                new BigDecimal("15000"),
                null
        ));
        approveQuotationUseCase.execute(new ApproveQuotationCommand(quotation.quotationId()));
        return createOrderFromQuotationUseCase.execute(new CreateOrderFromQuotationCommand(
                quotation.quotationId(),
                description,
                confirmationDate,
                promisedDeliveryDate,
                null
        )).orderId();
    }
}
