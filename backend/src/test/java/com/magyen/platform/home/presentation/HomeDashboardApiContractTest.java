package com.magyen.platform.home.presentation;

import com.magyen.platform.commercial.domain.DeliveryCommitment;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderItem;
import com.magyen.platform.commercial.domain.OrderNumber;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.ProductSpecification;
import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationCommand;
import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationOccurrenceCommand;
import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationOccurrenceResult;
import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationResult;
import com.magyen.platform.finance.application.dto.RegisterFinancialTransactionCommand;
import com.magyen.platform.finance.application.dto.RegisterPaymentCommand;
import com.magyen.platform.finance.application.usecase.CreateRecurringFinancialObligationOccurrenceUseCase;
import com.magyen.platform.finance.application.usecase.CreateRecurringFinancialObligationUseCase;
import com.magyen.platform.finance.application.usecase.RegisterFinancialTransactionUseCase;
import com.magyen.platform.finance.application.usecase.RegisterPaymentUseCase;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.finance.domain.RecurringObligationFrequency;
import com.magyen.platform.finance.domain.RecurringObligationType;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemCommand;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemResult;
import com.magyen.platform.inventory.application.usecase.CreateInventoryItemUseCase;
import com.magyen.platform.shared.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class HomeDashboardApiContractTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 8, 10);

    @MockitoBean
    private Clock clock;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private RegisterFinancialTransactionUseCase registerFinancialTransactionUseCase;

    @Autowired
    private RegisterPaymentUseCase registerPaymentUseCase;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CreateRecurringFinancialObligationUseCase createObligationUseCase;

    @Autowired
    private CreateRecurringFinancialObligationOccurrenceUseCase createOccurrenceUseCase;

    @Autowired
    private CreateInventoryItemUseCase createInventoryItemUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ZoneId zone = ZoneId.systemDefault();
        when(clock.getZone()).thenReturn(zone);
        when(clock.instant()).thenReturn(TODAY.atStartOfDay(zone).toInstant());
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void dashboardWithoutDatesUsesCurrentCalendarMonthAndReceivablesObject() throws Exception {
        mockMvc.perform(get("/api/v1/home/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromDate").value("2026-08-01"))
                .andExpect(jsonPath("$.toDate").value("2026-08-31"))
                .andExpect(jsonPath("$.generatedAt").exists())
                .andExpect(jsonPath("$.financialSummary").exists())
                .andExpect(jsonPath("$.financialSummary.income").isNumber())
                .andExpect(jsonPath("$.financialSummary.expense").isNumber())
                .andExpect(jsonPath("$.financialSummary.netResult").isNumber())
                .andExpect(jsonPath("$.financialSummary.transactionCount").isNumber())
                .andExpect(jsonPath("$.receivables").exists())
                .andExpect(jsonPath("$.receivables.totalOutstandingAmount").isNumber())
                .andExpect(jsonPath("$.receivables.totalCollectedAmount").isNumber())
                .andExpect(jsonPath("$.receivables.orderCount").isNumber())
                .andExpect(jsonPath("$.receivables.items").isArray())
                .andExpect(jsonPath("$.completedReceivables").exists())
                .andExpect(jsonPath("$.completedReceivables.totalOutstandingAmount").isNumber())
                .andExpect(jsonPath("$.completedReceivables.totalCollectedAmount").isNumber())
                .andExpect(jsonPath("$.completedReceivables.orderCount").isNumber())
                .andExpect(jsonPath("$.completedReceivables.items").isArray())
                .andExpect(jsonPath("$.commitments").exists())
                .andExpect(jsonPath("$.commitments.totalPendingAmount").isNumber())
                .andExpect(jsonPath("$.commitments.totalOverdueAmount").isNumber())
                .andExpect(jsonPath("$.commitments.overdueCount").isNumber())
                .andExpect(jsonPath("$.commitments.upcomingCount").isNumber())
                .andExpect(jsonPath("$.commitments.items").isArray())
                .andExpect(jsonPath("$.inventoryAlerts").exists())
                .andExpect(jsonPath("$.inventoryAlerts.lowStockCount").isNumber())
                .andExpect(jsonPath("$.inventoryAlerts.items").isArray())
                .andExpect(jsonPath("$.paperRollAlerts").exists())
                .andExpect(jsonPath("$.paperRollAlerts.lowStockCount").isNumber())
                .andExpect(jsonPath("$.paperRollAlerts.items").isArray())
                .andExpect(jsonPath("$.productionSummary").exists())
                .andExpect(jsonPath("$.productionSummary.totalOrders").isNumber())
                .andExpect(jsonPath("$.productionSummary.createdCount").isNumber())
                .andExpect(jsonPath("$.productionSummary.plannedCount").isNumber())
                .andExpect(jsonPath("$.productionSummary.inProgressCount").isNumber())
                .andExpect(jsonPath("$.productionSummary.completedCount").isNumber())
                .andExpect(jsonPath("$.productionSummary.items").isArray())
                .andExpect(jsonPath("$.profitabilitySummary").exists())
                .andExpect(jsonPath("$.profitabilitySummary.evaluatedOrderCount").isNumber())
                .andExpect(jsonPath("$.profitabilitySummary.completeOrderCount").isNumber())
                .andExpect(jsonPath("$.profitabilitySummary.partiallyUnvaluedOrderCount").isNumber())
                .andExpect(jsonPath("$.profitabilitySummary.noCostDataOrderCount").isNumber())
                .andExpect(jsonPath("$.profitabilitySummary.totalOrderValue").isNumber())
                .andExpect(jsonPath("$.profitabilitySummary.totalDirectCost").isNumber())
                .andExpect(jsonPath("$.profitabilitySummary.totalDirectProfit").isNumber())
                .andExpect(jsonPath("$.profitabilitySummary.unvaluedCostCount").isNumber());
    }

    @Test
    void dashboardExposesCommitmentsStructureWithPendingOccurrence() throws Exception {
        CreateRecurringFinancialObligationResult obligation = createObligationUseCase.execute(
                new CreateRecurringFinancialObligationCommand(
                        "Home-API-Commit-" + suffix(),
                        RecurringObligationType.SERVICE,
                        new BigDecimal("150000.00"),
                        RecurringObligationFrequency.MONTHLY,
                        15,
                        LocalDate.of(2026, 8, 1),
                        null,
                        null,
                        null
                )
        );
        CreateRecurringFinancialObligationOccurrenceResult occurrence = createOccurrenceUseCase.execute(
                new CreateRecurringFinancialObligationOccurrenceCommand(
                        obligation.obligationId(),
                        LocalDate.of(2026, 8, 15),
                        null
                )
        );

        mockMvc.perform(get("/api/v1/home/dashboard")
                        .param("fromDate", "2099-01-01")
                        .param("toDate", "2099-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.financialSummary.income").value(0))
                .andExpect(jsonPath("$.commitments.items[*].occurrenceId").value(
                        hasItem(occurrence.occurrenceId().toString())
                ))
                .andExpect(jsonPath("$.commitments.items[?(@.occurrenceId=='"
                        + occurrence.occurrenceId() + "')].expectedAmount").value(hasItem(150000.00)))
                .andExpect(jsonPath("$.commitments.items[?(@.occurrenceId=='"
                        + occurrence.occurrenceId() + "')].status").value(hasItem("PENDING")))
                .andExpect(jsonPath("$.commitments.items[?(@.occurrenceId=='"
                        + occurrence.occurrenceId() + "')].overdue").value(hasItem(false)))
                .andExpect(jsonPath("$.commitments.totalPendingAmount").isNumber())
                .andExpect(jsonPath("$.commitments.totalOverdueAmount").isNumber())
                .andExpect(jsonPath("$.commitments.overdueCount").isNumber())
                .andExpect(jsonPath("$.commitments.upcomingCount").isNumber());
    }

    @Test
    void dashboardExposesReceivablesStructureWithOutstandingOrder() throws Exception {
        Order order = createOrderWithTotal("ORD-HOME-API-", "1000000.00");
        registerPaymentUseCase.execute(new RegisterPaymentCommand(
                order.getId(),
                new BigDecimal("400000.00"),
                LocalDate.of(2026, 8, 5),
                "Abono API Home"
        ));

        mockMvc.perform(get("/api/v1/home/dashboard")
                        .param("fromDate", "2099-01-01")
                        .param("toDate", "2099-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.financialSummary.income").value(0))
                .andExpect(jsonPath("$.financialSummary.transactionCount").value(0))
                .andExpect(jsonPath("$.receivables.items[*].orderId").value(
                        hasItem(order.getId().toString())
                ))
                .andExpect(jsonPath("$.receivables.items[?(@.orderId=='" + order.getId()
                        + "')].orderValue").value(hasItem(1000000.00)))
                .andExpect(jsonPath("$.receivables.items[?(@.orderId=='" + order.getId()
                        + "')].collectedAmount").value(hasItem(400000.00)))
                .andExpect(jsonPath("$.receivables.items[?(@.orderId=='" + order.getId()
                        + "')].outstandingAmount").value(hasItem(600000.00)))
                .andExpect(jsonPath("$.receivables.items[?(@.orderId=='" + order.getId()
                        + "')].orderNumber").value(hasItem(order.getOrderNumber().getValue())))
                .andExpect(jsonPath("$.receivables.items[?(@.orderId=='" + order.getId()
                        + "')].customerId").value(hasItem(order.getCustomerId().toString())));
    }

    @Test
    void dashboardWithExplicitDateRangeIntegratesFinanceSummary() throws Exception {
        registerFinancialTransactionUseCase.execute(
                new RegisterFinancialTransactionCommand(
                        FinancialTransactionType.INCOME,
                        new BigDecimal("4500000.00"),
                        LocalDate.of(2026, 8, 4),
                        "SALES",
                        "Home API income " + suffix(),
                        null,
                        null,
                        null
                )
        );
        registerFinancialTransactionUseCase.execute(
                new RegisterFinancialTransactionCommand(
                        FinancialTransactionType.EXPENSE,
                        new BigDecimal("1500000.00"),
                        LocalDate.of(2026, 8, 12),
                        "SERVICES",
                        "Home API expense " + suffix(),
                        null,
                        null,
                        null
                )
        );

        mockMvc.perform(get("/api/v1/home/dashboard")
                        .param("fromDate", "2026-08-01")
                        .param("toDate", "2026-08-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromDate").value("2026-08-01"))
                .andExpect(jsonPath("$.toDate").value("2026-08-31"))
                .andExpect(jsonPath("$.financialSummary.income").value(greaterThanOrEqualTo(4500000.00)))
                .andExpect(jsonPath("$.financialSummary.expense").value(greaterThanOrEqualTo(1500000.00)))
                .andExpect(jsonPath("$.financialSummary.transactionCount").value(greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.receivables").exists())
                .andExpect(jsonPath("$.receivables.items").isArray())
                .andExpect(jsonPath("$.commitments").exists())
                .andExpect(jsonPath("$.commitments.items").isArray());
    }

    @Test
    void emptyPeriodReturnsZeroFinanceSummary() throws Exception {
        mockMvc.perform(get("/api/v1/home/dashboard")
                        .param("fromDate", "2099-01-01")
                        .param("toDate", "2099-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.financialSummary.income").value(0))
                .andExpect(jsonPath("$.financialSummary.expense").value(0))
                .andExpect(jsonPath("$.financialSummary.netResult").value(0))
                .andExpect(jsonPath("$.financialSummary.transactionCount").value(0))
                .andExpect(jsonPath("$.receivables").exists())
                .andExpect(jsonPath("$.receivables.items").isArray())
                .andExpect(jsonPath("$.commitments").exists())
                .andExpect(jsonPath("$.commitments.items").isArray());
    }

    @Test
    void invalidDateRangeReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/home/dashboard")
                        .param("fromDate", "2026-08-31")
                        .param("toDate", "2026-08-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidDateFormatReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/home/dashboard")
                        .param("fromDate", "not-a-date")
                        .param("toDate", "2026-08-31"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void onlyOneDateProvidedReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/home/dashboard").param("fromDate", "2026-08-01"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/home/dashboard").param("toDate", "2026-08-31"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void dashboardExposesInventoryAndPaperRollAlertSections() throws Exception {
        CreateInventoryItemResult fabric = createInventoryItemUseCase.execute(
                new CreateInventoryItemCommand(
                        "HOME-API-FAB-" + suffix(),
                        "Tela API Home",
                        "TELAS",
                        "METER",
                        new BigDecimal("5.0000"),
                        new BigDecimal("10.0000"),
                        "Tela low stock",
                        new BigDecimal("1000.00"),
                        "FABRIC",
                        false
                )
        );
        CreateInventoryItemResult roll = createInventoryItemUseCase.execute(
                new CreateInventoryItemCommand(
                        "HOME-API-RP-" + suffix(),
                        "Papel API Home",
                        "PLOTTER",
                        "METER",
                        new BigDecimal("8.0000"),
                        new BigDecimal("20.0000"),
                        "Rollo low stock",
                        new BigDecimal("4500.00"),
                        "PAPER",
                        true
                )
        );

        mockMvc.perform(get("/api/v1/home/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inventoryAlerts.items[*].inventoryItemId").value(
                        hasItem(fabric.inventoryItemId().toString())
                ))
                .andExpect(jsonPath("$.inventoryAlerts.items[*].inventoryItemId").value(
                        hasItem(roll.inventoryItemId().toString())
                ))
                .andExpect(jsonPath("$.paperRollAlerts.items[*].inventoryItemId").value(
                        hasItem(roll.inventoryItemId().toString())
                ))
                .andExpect(jsonPath("$.paperRollAlerts.items[*].inventoryItemId",
                        org.hamcrest.Matchers.not(hasItem(fabric.inventoryItemId().toString()))))
                .andExpect(jsonPath("$.paperRollAlerts.items[?(@.inventoryItemId=='"
                        + roll.inventoryItemId() + "')].paperRollNumber").value(hasItem(roll.paperRollNumber())))
                .andExpect(jsonPath("$.inventoryAlerts.lowStockCount").value(greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.paperRollAlerts.lowStockCount").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.productionSummary").exists())
                .andExpect(jsonPath("$.profitabilitySummary").exists());
    }

    private Order createOrderWithTotal(String numberPrefix, String unitPrice) {
        LocalDate confirmationDate = LocalDate.of(2026, 8, 1);
        OrderItem item = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Producto Home API AR",
                1,
                "Tela",
                "Negro",
                Money.of(new BigDecimal(unitPrice)),
                ProductSpecification.empty(),
                List.of()
        );

        Order order = Order.create(
                OrderNumber.of(numberPrefix + suffix()),
                UUID.randomUUID(),
                UUID.randomUUID(),
                confirmationDate,
                DeliveryCommitment.of(confirmationDate.plusDays(7)),
                UUID.randomUUID(),
                "Orden Home API receivables",
                List.of(item)
        );

        return orderRepository.save(order);
    }

    private static String suffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
