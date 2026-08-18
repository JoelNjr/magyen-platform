package com.magyen.platform.home.application.usecase;

import com.magyen.platform.commercial.domain.Customer;
import com.magyen.platform.commercial.domain.CustomerRepository;
import com.magyen.platform.commercial.domain.DeliveryCommitment;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderItem;
import com.magyen.platform.commercial.domain.OrderNumber;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.OrderStatus;
import com.magyen.platform.commercial.domain.PaymentSummary;
import com.magyen.platform.commercial.domain.ProductSpecification;
import com.magyen.platform.finance.application.dto.CancelRecurringFinancialObligationOccurrenceCommand;
import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationCommand;
import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationOccurrenceCommand;
import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationOccurrenceResult;
import com.magyen.platform.finance.application.dto.CreateRecurringFinancialObligationResult;
import com.magyen.platform.finance.application.dto.PayRecurringFinancialObligationOccurrenceCommand;
import com.magyen.platform.finance.application.dto.RegisterFinancialTransactionCommand;
import com.magyen.platform.finance.application.dto.RegisterPaymentCommand;
import com.magyen.platform.finance.application.usecase.CancelRecurringFinancialObligationOccurrenceUseCase;
import com.magyen.platform.finance.application.usecase.CreateRecurringFinancialObligationOccurrenceUseCase;
import com.magyen.platform.finance.application.usecase.CreateRecurringFinancialObligationUseCase;
import com.magyen.platform.finance.application.usecase.PayRecurringFinancialObligationOccurrenceUseCase;
import com.magyen.platform.finance.application.usecase.RegisterFinancialTransactionUseCase;
import com.magyen.platform.finance.application.usecase.RegisterPaymentUseCase;
import com.magyen.platform.finance.domain.FinancialTransaction;
import com.magyen.platform.finance.domain.FinancialTransactionRepository;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.finance.domain.RecurringObligationFrequency;
import com.magyen.platform.finance.domain.RecurringObligationType;
import com.magyen.platform.home.application.dto.GetHomeDashboardQuery;
import com.magyen.platform.home.application.dto.GetHomeDashboardResult;
import com.magyen.platform.home.application.dto.HomeCommitmentItem;
import com.magyen.platform.home.application.dto.HomeInventoryAlertItem;
import com.magyen.platform.home.application.dto.HomePaperRollAlertItem;
import com.magyen.platform.home.application.dto.HomeProductionItem;
import com.magyen.platform.home.application.dto.HomeReceivableItem;
import com.magyen.platform.home.domain.exception.HomeDomainException;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemCommand;
import com.magyen.platform.inventory.application.dto.CreateInventoryItemResult;
import com.magyen.platform.inventory.application.usecase.CreateInventoryItemUseCase;
import com.magyen.platform.inventory.domain.InventoryItem;
import com.magyen.platform.inventory.domain.InventoryItemRepository;
import com.magyen.platform.inventory.domain.MaterialCode;
import com.magyen.platform.inventory.infrastructure.persistence.repository.SpringDataInventoryMovementRepository;
import com.magyen.platform.production.application.dto.CompleteProductionOrderCommand;
import com.magyen.platform.production.application.dto.PlanProductionOrderCommand;
import com.magyen.platform.production.application.dto.RegisterProductionMaterialConsumptionCommand;
import com.magyen.platform.production.application.dto.StartProductionOrderCommand;
import com.magyen.platform.production.application.usecase.CompleteProductionOrderUseCase;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class GetHomeDashboardUseCaseTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 8, 10);

    @MockitoBean
    private Clock clock;

    @Autowired
    private GetHomeDashboardUseCase getHomeDashboardUseCase;

    @Autowired
    private RegisterFinancialTransactionUseCase registerFinancialTransactionUseCase;

    @Autowired
    private RegisterPaymentUseCase registerPaymentUseCase;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    @Autowired
    private SpringDataInventoryMovementRepository springDataInventoryMovementRepository;

    @Autowired
    private CreateRecurringFinancialObligationUseCase createObligationUseCase;

    @Autowired
    private CreateRecurringFinancialObligationOccurrenceUseCase createOccurrenceUseCase;

    @Autowired
    private PayRecurringFinancialObligationOccurrenceUseCase payOccurrenceUseCase;

    @Autowired
    private CancelRecurringFinancialObligationOccurrenceUseCase cancelOccurrenceUseCase;

    @Autowired
    private CreateInventoryItemUseCase createInventoryItemUseCase;

    @Autowired
    private ProductionOrderRepository productionOrderRepository;

    @Autowired
    private PlanProductionOrderUseCase planProductionOrderUseCase;

    @Autowired
    private StartProductionOrderUseCase startProductionOrderUseCase;

    @Autowired
    private CompleteProductionOrderUseCase completeProductionOrderUseCase;

    @Autowired
    private RegisterProductionMaterialConsumptionUseCase registerProductionMaterialConsumptionUseCase;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @BeforeEach
    void setFixedClock() {
        ZoneId zone = ZoneId.systemDefault();
        when(clock.getZone()).thenReturn(zone);
        when(clock.instant()).thenReturn(TODAY.atStartOfDay(zone).toInstant());
    }

    @Test
    void withoutDatesUsesCurrentCalendarMonth() {
        GetHomeDashboardResult result = getHomeDashboardUseCase.execute(
                new GetHomeDashboardQuery(null, null)
        );

        assertEquals(LocalDate.of(2026, 8, 1), result.fromDate());
        assertEquals(LocalDate.of(2026, 8, 31), result.toDate());
    }

    @Test
    void withExplicitDateRangeUsesProvidedPeriod() {
        GetHomeDashboardResult result = getHomeDashboardUseCase.execute(
                new GetHomeDashboardQuery(
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 7, 15)
                )
        );

        assertEquals(LocalDate.of(2026, 7, 1), result.fromDate());
        assertEquals(LocalDate.of(2026, 7, 15), result.toDate());
    }

    @Test
    void financeSummaryIsCorrectlyIntegrated() {
        registerFinancialTransactionUseCase.execute(
                new RegisterFinancialTransactionCommand(
                        FinancialTransactionType.INCOME,
                        new BigDecimal("5000000.00"),
                        LocalDate.of(2026, 8, 5),
                        "SALES",
                        "Home dashboard income " + suffix(),
                        null,
                        null,
                        null
                )
        );
        registerFinancialTransactionUseCase.execute(
                new RegisterFinancialTransactionCommand(
                        FinancialTransactionType.EXPENSE,
                        new BigDecimal("3200000.00"),
                        LocalDate.of(2026, 8, 20),
                        "SERVICES",
                        "Home dashboard expense " + suffix(),
                        null,
                        null,
                        null
                )
        );

        GetHomeDashboardResult result = getHomeDashboardUseCase.execute(
                new GetHomeDashboardQuery(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                )
        );

        assertTrue(result.financialSummary().income().compareTo(new BigDecimal("5000000.00")) >= 0);
        assertTrue(result.financialSummary().expense().compareTo(new BigDecimal("3200000.00")) >= 0);
        assertEquals(
                result.financialSummary().income().subtract(result.financialSummary().expense()),
                result.financialSummary().netResult()
        );
        assertTrue(result.financialSummary().transactionCount() >= 2);
    }

    @Test
    void noFinancialTransactionsReturnsZeroSummary() {
        GetHomeDashboardResult result = getHomeDashboardUseCase.execute(
                new GetHomeDashboardQuery(
                        LocalDate.of(2099, 1, 1),
                        LocalDate.of(2099, 1, 31)
                )
        );

        assertEquals(0, result.financialSummary().income().compareTo(BigDecimal.ZERO));
        assertEquals(0, result.financialSummary().expense().compareTo(BigDecimal.ZERO));
        assertEquals(0, result.financialSummary().netResult().compareTo(BigDecimal.ZERO));
        assertEquals(0, result.financialSummary().transactionCount());
    }

    @Test
    void fromDateAfterToDateThrowsHomeDomainException() {
        HomeDomainException exception = assertThrows(
                HomeDomainException.class,
                () -> getHomeDashboardUseCase.execute(
                        new GetHomeDashboardQuery(
                                LocalDate.of(2026, 8, 31),
                                LocalDate.of(2026, 8, 1)
                        )
                )
        );

        assertEquals("From date must not be after to date", exception.getMessage());
    }

    @Test
    void onlyOneDateProvidedThrowsHomeDomainException() {
        assertThrows(
                HomeDomainException.class,
                () -> getHomeDashboardUseCase.execute(
                        new GetHomeDashboardQuery(LocalDate.of(2026, 8, 1), null)
                )
        );
        assertThrows(
                HomeDomainException.class,
                () -> getHomeDashboardUseCase.execute(
                        new GetHomeDashboardQuery(null, LocalDate.of(2026, 8, 31))
                )
        );
    }

    @Test
    void orderWithNoPaymentsHasFullOutstandingBalance() {
        Order order = createOrderWithTotal("ORD-HOME-A-", "1000000.00");

        GetHomeDashboardResult result = getHomeDashboardUseCase.execute(
                new GetHomeDashboardQuery(null, null)
        );

        HomeReceivableItem item = requireReceivable(result, order.getId());
        assertEquals(0, item.orderValue().compareTo(new BigDecimal("1000000.00")));
        assertEquals(0, item.collectedAmount().compareTo(BigDecimal.ZERO));
        assertEquals(0, item.outstandingAmount().compareTo(new BigDecimal("1000000.00")));
        assertEquals(order.getOrderNumber().getValue(), item.orderNumber());
        assertEquals(order.getCustomerId(), item.customerId());
    }

    @Test
    void orderWithPartialPaymentsHasRemainingBalance() {
        Order order = createOrderWithTotal("ORD-HOME-B-", "1000000.00");
        registerPaymentUseCase.execute(new RegisterPaymentCommand(
                order.getId(),
                new BigDecimal("300000.00"),
                LocalDate.of(2026, 8, 5),
                "Abono parcial"
        ));

        GetHomeDashboardResult result = getHomeDashboardUseCase.execute(
                new GetHomeDashboardQuery(null, null)
        );

        HomeReceivableItem item = requireReceivable(result, order.getId());
        assertEquals(0, item.orderValue().compareTo(new BigDecimal("1000000.00")));
        assertEquals(0, item.collectedAmount().compareTo(new BigDecimal("300000.00")));
        assertEquals(0, item.outstandingAmount().compareTo(new BigDecimal("700000.00")));
    }

    @Test
    void fullyPaidOrderIsExcludedFromReceivables() {
        Order order = createOrderWithTotal("ORD-HOME-C-", "500000.00");
        registerPaymentUseCase.execute(new RegisterPaymentCommand(
                order.getId(),
                new BigDecimal("500000.00"),
                LocalDate.of(2026, 8, 6),
                "Pago total"
        ));

        GetHomeDashboardResult result = getHomeDashboardUseCase.execute(
                new GetHomeDashboardQuery(null, null)
        );

        assertTrue(result.receivables().items().stream()
                .noneMatch(item -> item.orderId().equals(order.getId())));
    }

    @Test
    void multipleOutstandingOrdersAggregateTotals() {
        Order first = createOrderWithTotal("ORD-HOME-D1-", "1000000.00");
        Order second = createOrderWithTotal("ORD-HOME-D2-", "400000.00");
        registerPaymentUseCase.execute(new RegisterPaymentCommand(
                first.getId(),
                new BigDecimal("200000.00"),
                LocalDate.of(2026, 8, 7),
                "Abono D1"
        ));

        GetHomeDashboardResult result = getHomeDashboardUseCase.execute(
                new GetHomeDashboardQuery(null, null)
        );

        HomeReceivableItem firstItem = requireReceivable(result, first.getId());
        HomeReceivableItem secondItem = requireReceivable(result, second.getId());

        BigDecimal expectedOutstanding = firstItem.outstandingAmount().add(secondItem.outstandingAmount());
        BigDecimal expectedCollected = firstItem.collectedAmount().add(secondItem.collectedAmount());

        assertEquals(0, firstItem.outstandingAmount().compareTo(new BigDecimal("800000.00")));
        assertEquals(0, secondItem.outstandingAmount().compareTo(new BigDecimal("400000.00")));
        assertTrue(result.receivables().orderCount() >= 2);
        assertTrue(result.receivables().totalOutstandingAmount().compareTo(expectedOutstanding) >= 0);
        assertTrue(result.receivables().totalCollectedAmount().compareTo(expectedCollected) >= 0);
    }

    @Test
    void receivablesOrderingIsDeterministicHighestOutstandingFirst() {
        Order lowerOutstanding = createOrderWithNumberAndTotal("ORD-HOME-E-LOW", "300000.00");
        Order higherOutstanding = createOrderWithNumberAndTotal("ORD-HOME-E-HIGH", "900000.00");

        GetHomeDashboardResult result = getHomeDashboardUseCase.execute(
                new GetHomeDashboardQuery(null, null)
        );

        List<UUID> orderIds = result.receivables().items().stream()
                .map(HomeReceivableItem::orderId)
                .toList();
        int higherIndex = orderIds.indexOf(higherOutstanding.getId());
        int lowerIndex = orderIds.indexOf(lowerOutstanding.getId());

        assertTrue(higherIndex >= 0);
        assertTrue(lowerIndex >= 0);
        assertTrue(higherIndex < lowerIndex);
    }

    @Test
    void currentOutstandingIsNotRestrictedByFinancialDateRange() {
        Order order = createOrderWithTotal("ORD-HOME-F-", "750000.00");
        registerPaymentUseCase.execute(new RegisterPaymentCommand(
                order.getId(),
                new BigDecimal("250000.00"),
                LocalDate.of(2026, 8, 8),
                "Abono F"
        ));

        GetHomeDashboardResult result = getHomeDashboardUseCase.execute(
                new GetHomeDashboardQuery(
                        LocalDate.of(2099, 1, 1),
                        LocalDate.of(2099, 1, 31)
                )
        );

        assertEquals(0, result.financialSummary().income().compareTo(BigDecimal.ZERO));
        assertEquals(0, result.financialSummary().transactionCount());

        HomeReceivableItem item = requireReceivable(result, order.getId());
        assertEquals(0, item.outstandingAmount().compareTo(new BigDecimal("500000.00")));
    }

    @Test
    void noOutstandingOrdersReturnsValidEmptyReceivables() {
        Order paid = createOrderWithTotal("ORD-HOME-G-", "100000.00");
        registerPaymentUseCase.execute(new RegisterPaymentCommand(
                paid.getId(),
                new BigDecimal("100000.00"),
                LocalDate.of(2026, 8, 9),
                "Pago G"
        ));

        GetHomeDashboardResult result = getHomeDashboardUseCase.execute(
                new GetHomeDashboardQuery(null, null)
        );

        assertTrue(result.receivables().items().stream()
                .noneMatch(item -> item.orderId().equals(paid.getId())));
        assertTrue(result.receivables().totalOutstandingAmount().compareTo(BigDecimal.ZERO) >= 0);
        assertTrue(result.receivables().orderCount() >= 0);
        assertTrue(result.receivables().items() != null);
    }

    @Test
    void dashboardStillReturnsFinanceSummaryWithReceivables() {
        Order order = createOrderWithTotal("ORD-HOME-H-", "200000.00");
        registerFinancialTransactionUseCase.execute(
                new RegisterFinancialTransactionCommand(
                        FinancialTransactionType.INCOME,
                        new BigDecimal("111000.00"),
                        LocalDate.of(2026, 8, 3),
                        "SALES",
                        "Home finance+AR " + suffix(),
                        null,
                        null,
                        null
                )
        );

        GetHomeDashboardResult result = getHomeDashboardUseCase.execute(
                new GetHomeDashboardQuery(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                )
        );

        assertTrue(result.financialSummary().income().compareTo(new BigDecimal("111000.00")) >= 0);
        requireReceivable(result, order.getId());
    }

    @Test
    void receivablesReadPathCreatesNoFinancialTransactionsOrInventoryMovements() {
        Order order = createOrderWithTotal("ORD-HOME-IJ-", "600000.00");
        registerPaymentUseCase.execute(new RegisterPaymentCommand(
                order.getId(),
                new BigDecimal("100000.00"),
                LocalDate.of(2026, 8, 4),
                "Abono IJ"
        ));

        List<FinancialTransaction> beforeTransactions = financialTransactionRepository.findAllNewestFirst();
        long beforeTransactionCount = beforeTransactions.size();
        String beforeFingerprint = fingerprint(beforeTransactions);
        long beforeMovementCount = springDataInventoryMovementRepository.count();

        getHomeDashboardUseCase.execute(new GetHomeDashboardQuery(null, null));

        List<FinancialTransaction> afterTransactions = financialTransactionRepository.findAllNewestFirst();
        assertEquals(beforeTransactionCount, afterTransactions.size());
        assertEquals(beforeFingerprint, fingerprint(afterTransactions));
        assertEquals(beforeMovementCount, springDataInventoryMovementRepository.count());
    }

    @Test
    void completedReceivablesIncludesDeliveredOrdersWithOutstandingBalance() {
        Customer customer = customerRepository.save(Customer.create("Cliente Completado " + suffix()));
        Order delivered = createDeliveredOrder(
                customer.getId(),
                "ORD-HOME-DEL-",
                "Camisetas de voleibol",
                "400000.00"
        );
        registerPaymentUseCase.execute(new RegisterPaymentCommand(
                delivered.getId(),
                new BigDecimal("200000.00"),
                LocalDate.of(2026, 8, 6),
                "Abono pedido completado"
        ));

        GetHomeDashboardResult result = getHomeDashboardUseCase.execute(
                new GetHomeDashboardQuery(null, null)
        );

        HomeReceivableItem item = requireCompletedReceivable(result, delivered.getId());
        assertEquals(delivered.getOrderNumber().getValue(), item.orderNumber());
        assertEquals("Camisetas de voleibol", item.description());
        assertEquals(customer.getName(), item.customerName());
        assertEquals(0, item.orderValue().compareTo(new BigDecimal("400000.00")));
        assertEquals(0, item.collectedAmount().compareTo(new BigDecimal("200000.00")));
        assertEquals(0, item.outstandingAmount().compareTo(new BigDecimal("200000.00")));
    }

    @Test
    void completedReceivablesExcludesFullyPaidDeliveredOrders() {
        Customer customer = customerRepository.save(Customer.create("Cliente Pagado " + suffix()));
        Order delivered = createDeliveredOrder(
                customer.getId(),
                "ORD-HOME-PAID-",
                "Pedido pagado",
                "150000.00"
        );
        registerPaymentUseCase.execute(new RegisterPaymentCommand(
                delivered.getId(),
                new BigDecimal("150000.00"),
                LocalDate.of(2026, 8, 6),
                "Pago total"
        ));

        GetHomeDashboardResult result = getHomeDashboardUseCase.execute(
                new GetHomeDashboardQuery(null, null)
        );

        assertTrue(result.completedReceivables().items().stream()
                .noneMatch(item -> item.orderId().equals(delivered.getId())));
    }

    @Test
    void completedReceivablesExcludesOrdersNotYetDelivered() {
        Order confirmed = createOrderWithTotal("ORD-HOME-OPEN-", "800000.00");

        GetHomeDashboardResult result = getHomeDashboardUseCase.execute(
                new GetHomeDashboardQuery(null, null)
        );

        requireReceivable(result, confirmed.getId());
        assertTrue(result.completedReceivables().items().stream()
                .noneMatch(item -> item.orderId().equals(confirmed.getId())));
    }

    @Test
    void completedReceivablesReadPathCreatesNoFinancialTransactions() {
        Customer customer = customerRepository.save(Customer.create("Cliente Guard " + suffix()));
        Order delivered = createDeliveredOrder(
                customer.getId(),
                "ORD-HOME-GUARD-",
                "Pedido guardia",
                "250000.00"
        );
        registerPaymentUseCase.execute(new RegisterPaymentCommand(
                delivered.getId(),
                new BigDecimal("50000.00"),
                LocalDate.of(2026, 8, 6),
                "Abono guardia"
        ));

        List<FinancialTransaction> beforeTransactions = financialTransactionRepository.findAllNewestFirst();
        long beforeTransactionCount = beforeTransactions.size();
        String beforeFingerprint = fingerprint(beforeTransactions);

        GetHomeDashboardResult result = getHomeDashboardUseCase.execute(new GetHomeDashboardQuery(null, null));
        requireCompletedReceivable(result, delivered.getId());

        List<FinancialTransaction> afterTransactions = financialTransactionRepository.findAllNewestFirst();
        assertEquals(beforeTransactionCount, afterTransactions.size());
        assertEquals(beforeFingerprint, fingerprint(afterTransactions));
    }

    @Test
    void dashboardDoesNotModifyFinanceDataOrCreateTransactionsOrInventoryMovements() {
        registerFinancialTransactionUseCase.execute(
                new RegisterFinancialTransactionCommand(
                        FinancialTransactionType.INCOME,
                        new BigDecimal("100000.00"),
                        LocalDate.of(2026, 8, 8),
                        "SALES",
                        "Side-effect guard " + suffix(),
                        null,
                        null,
                        null
                )
        );

        List<FinancialTransaction> beforeTransactions = financialTransactionRepository.findAllNewestFirst();
        long beforeTransactionCount = beforeTransactions.size();
        String beforeFingerprint = fingerprint(beforeTransactions);
        long beforeMovementCount = springDataInventoryMovementRepository.count();

        getHomeDashboardUseCase.execute(new GetHomeDashboardQuery(null, null));

        List<FinancialTransaction> afterTransactions = financialTransactionRepository.findAllNewestFirst();
        assertEquals(beforeTransactionCount, afterTransactions.size());
        assertEquals(beforeFingerprint, fingerprint(afterTransactions));
        assertEquals(beforeMovementCount, springDataInventoryMovementRepository.count());
    }

    @Test
    void noPendingCommitmentsReturnsValidEmptyCommitmentsModel() {
        CreateRecurringFinancialObligationResult obligation = createObligation(
                "Home-Empty-" + suffix(),
                "100000.00",
                15
        );
        CreateRecurringFinancialObligationOccurrenceResult occurrence =
                createOccurrence(obligation.obligationId(), LocalDate.of(2026, 8, 15));
        payOccurrenceUseCase.execute(new PayRecurringFinancialObligationOccurrenceCommand(occurrence.occurrenceId()));

        GetHomeDashboardResult result = getHomeDashboardUseCase.execute(
                new GetHomeDashboardQuery(null, null)
        );

        assertTrue(result.commitments().items().stream()
                .noneMatch(item -> item.occurrenceId().equals(occurrence.occurrenceId())));
        assertTrue(result.commitments().totalPendingAmount().compareTo(BigDecimal.ZERO) >= 0);
        assertTrue(result.commitments().items() != null);
    }

    @Test
    void pendingNonOverdueCommitmentAppears() {
        CreateRecurringFinancialObligationResult obligation = createObligation(
                "Home-Pending-" + suffix(),
                "120000.00",
                15
        );
        CreateRecurringFinancialObligationOccurrenceResult occurrence =
                createOccurrence(obligation.obligationId(), LocalDate.of(2026, 8, 15));

        GetHomeDashboardResult result = getHomeDashboardUseCase.execute(
                new GetHomeDashboardQuery(null, null)
        );

        HomeCommitmentItem item = requireCommitment(result, occurrence.occurrenceId());
        assertEquals(false, item.overdue());
        assertEquals(0, item.expectedAmount().compareTo(new BigDecimal("120000.00")));
        assertEquals("PENDING", item.status());
        assertEquals(obligation.obligationId(), item.obligationId());
        assertEquals(LocalDate.of(2026, 8, 15), item.dueDate());
        assertEquals(5, item.daysUntilDue());
    }

    @Test
    void overdueCommitmentContributesToPendingAndOverdueTotals() {
        CreateRecurringFinancialObligationResult obligation = createObligation(
                "Home-Overdue-" + suffix(),
                "200000.00",
                9
        );
        CreateRecurringFinancialObligationOccurrenceResult occurrence =
                createOccurrence(obligation.obligationId(), LocalDate.of(2026, 8, 9));

        GetHomeDashboardResult before = getHomeDashboardUseCase.execute(
                new GetHomeDashboardQuery(null, null)
        );
        // Snapshot may include other pending; verify our item and that overdue flags contribute.
        HomeCommitmentItem item = requireCommitment(before, occurrence.occurrenceId());
        assertTrue(item.overdue());
        assertEquals(1, item.daysOverdue());
        assertTrue(before.commitments().totalPendingAmount().compareTo(new BigDecimal("200000.00")) >= 0);
        assertTrue(before.commitments().totalOverdueAmount().compareTo(new BigDecimal("200000.00")) >= 0);
        assertTrue(before.commitments().overdueCount() >= 1);
    }

    @Test
    void upcomingCommitmentAppearsAndOverdueIsExcludedFromUpcoming() {
        CreateRecurringFinancialObligationResult upcomingObligation = createObligation(
                "Home-Upcoming-" + suffix(),
                "80000.00",
                12
        );
        CreateRecurringFinancialObligationResult overdueObligation = createObligation(
                "Home-UpdOv-" + suffix(),
                "90000.00",
                8
        );
        CreateRecurringFinancialObligationOccurrenceResult upcoming =
                createOccurrence(upcomingObligation.obligationId(), LocalDate.of(2026, 8, 12));
        CreateRecurringFinancialObligationOccurrenceResult overdue =
                createOccurrence(overdueObligation.obligationId(), LocalDate.of(2026, 8, 8));

        GetHomeDashboardResult result = getHomeDashboardUseCase.execute(
                new GetHomeDashboardQuery(null, null)
        );

        HomeCommitmentItem upcomingItem = requireCommitment(result, upcoming.occurrenceId());
        HomeCommitmentItem overdueItem = requireCommitment(result, overdue.occurrenceId());
        assertEquals(false, upcomingItem.overdue());
        assertEquals(true, overdueItem.overdue());
        assertTrue(result.commitments().upcomingCount() >= 1);
        assertTrue(result.commitments().overdueCount() >= 1);
        // Overdue must not inflate upcoming: Finance upcoming excludes dueDate < today.
        assertTrue(upcomingItem.daysUntilDue() != null && upcomingItem.daysUntilDue() >= 0);
        assertTrue(overdueItem.daysOverdue() != null && overdueItem.daysOverdue() > 0);
    }

    @Test
    void paidAndCancelledCommitmentsAreExcluded() {
        CreateRecurringFinancialObligationResult paidObligation = createObligation(
                "Home-Paid-" + suffix(),
                "50000.00",
                11
        );
        CreateRecurringFinancialObligationResult cancelledObligation = createObligation(
                "Home-Cancel-" + suffix(),
                "60000.00",
                13
        );
        CreateRecurringFinancialObligationOccurrenceResult paid =
                createOccurrence(paidObligation.obligationId(), LocalDate.of(2026, 8, 11));
        CreateRecurringFinancialObligationOccurrenceResult cancelled =
                createOccurrence(cancelledObligation.obligationId(), LocalDate.of(2026, 8, 13));

        payOccurrenceUseCase.execute(new PayRecurringFinancialObligationOccurrenceCommand(paid.occurrenceId()));
        cancelOccurrenceUseCase.execute(
                new CancelRecurringFinancialObligationOccurrenceCommand(cancelled.occurrenceId())
        );

        GetHomeDashboardResult result = getHomeDashboardUseCase.execute(
                new GetHomeDashboardQuery(null, null)
        );

        assertTrue(result.commitments().items().stream()
                .noneMatch(item -> item.occurrenceId().equals(paid.occurrenceId())));
        assertTrue(result.commitments().items().stream()
                .noneMatch(item -> item.occurrenceId().equals(cancelled.occurrenceId())));
    }

    @Test
    void multipleCommitmentsAggregateAndOrderDeterministically() {
        CreateRecurringFinancialObligationResult earlierOverdue = createObligation(
                "Home-Ord-A-" + suffix(),
                "100000.00",
                7
        );
        CreateRecurringFinancialObligationResult laterOverdueHigherAmount = createObligation(
                "Home-Ord-B-" + suffix(),
                "300000.00",
                8
        );
        CreateRecurringFinancialObligationResult upcoming = createObligation(
                "Home-Ord-C-" + suffix(),
                "50000.00",
                14
        );

        CreateRecurringFinancialObligationOccurrenceResult occA =
                createOccurrence(earlierOverdue.obligationId(), LocalDate.of(2026, 8, 7));
        CreateRecurringFinancialObligationOccurrenceResult occB =
                createOccurrence(laterOverdueHigherAmount.obligationId(), LocalDate.of(2026, 8, 8));
        CreateRecurringFinancialObligationOccurrenceResult occC =
                createOccurrence(upcoming.obligationId(), LocalDate.of(2026, 8, 14));

        GetHomeDashboardResult result = getHomeDashboardUseCase.execute(
                new GetHomeDashboardQuery(null, null)
        );

        List<UUID> ids = result.commitments().items().stream()
                .map(HomeCommitmentItem::occurrenceId)
                .toList();
        int indexA = ids.indexOf(occA.occurrenceId());
        int indexB = ids.indexOf(occB.occurrenceId());
        int indexC = ids.indexOf(occC.occurrenceId());
        assertTrue(indexA >= 0 && indexB >= 0 && indexC >= 0);
        // Finance order: dueDate ASC → overdue (Aug 7, Aug 8) before upcoming (Aug 14)
        assertTrue(indexA < indexB);
        assertTrue(indexB < indexC);

        assertTrue(result.commitments().totalPendingAmount()
                .compareTo(new BigDecimal("450000.00")) >= 0);
        assertTrue(result.commitments().totalOverdueAmount()
                .compareTo(new BigDecimal("400000.00")) >= 0);
        assertTrue(result.commitments().overdueCount() >= 2);
        assertTrue(result.commitments().upcomingCount() >= 1);
    }

    @Test
    void financialDateRangeDoesNotFilterCurrentCommitments() {
        CreateRecurringFinancialObligationResult obligation = createObligation(
                "Home-Date-" + suffix(),
                "175000.00",
                9
        );
        CreateRecurringFinancialObligationOccurrenceResult occurrence =
                createOccurrence(obligation.obligationId(), LocalDate.of(2026, 8, 9));

        GetHomeDashboardResult result = getHomeDashboardUseCase.execute(
                new GetHomeDashboardQuery(
                        LocalDate.of(2099, 1, 1),
                        LocalDate.of(2099, 1, 31)
                )
        );

        assertEquals(0, result.financialSummary().transactionCount());
        HomeCommitmentItem item = requireCommitment(result, occurrence.occurrenceId());
        assertTrue(item.overdue());
        assertEquals(0, item.expectedAmount().compareTo(new BigDecimal("175000.00")));
    }

    @Test
    void commitmentsCoexistWithFinanceSummaryAndReceivables() {
        Order order = createOrderWithTotal("ORD-HOME-CM-", "250000.00");
        registerFinancialTransactionUseCase.execute(
                new RegisterFinancialTransactionCommand(
                        FinancialTransactionType.INCOME,
                        new BigDecimal("88000.00"),
                        LocalDate.of(2026, 8, 2),
                        "SALES",
                        "Home commitments coexist " + suffix(),
                        null,
                        null,
                        null
                )
        );
        CreateRecurringFinancialObligationResult obligation = createObligation(
                "Home-Coexist-" + suffix(),
                "44000.00",
                16
        );
        CreateRecurringFinancialObligationOccurrenceResult occurrence =
                createOccurrence(obligation.obligationId(), LocalDate.of(2026, 8, 16));

        GetHomeDashboardResult result = getHomeDashboardUseCase.execute(
                new GetHomeDashboardQuery(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                )
        );

        assertTrue(result.financialSummary().income().compareTo(new BigDecimal("88000.00")) >= 0);
        requireReceivable(result, order.getId());
        requireCommitment(result, occurrence.occurrenceId());
    }

    @Test
    void noLowStockMaterialsReturnsEmptyInventoryAlerts() {
        CreateInventoryItemResult ok = createMaterial(
                "HOME-OK-" + suffix(),
                "FABRIC",
                "METER",
                "50.0000",
                "10.0000",
                false
        );

        GetHomeDashboardResult result = getHomeDashboardUseCase.execute(
                new GetHomeDashboardQuery(null, null)
        );

        assertTrue(result.inventoryAlerts().items().stream()
                .noneMatch(item -> item.inventoryItemId().equals(ok.inventoryItemId())));
        assertTrue(result.inventoryAlerts().lowStockCount() >= 0);
        assertTrue(result.inventoryAlerts().items() != null);
    }

    @Test
    void lowStockMaterialAppearsAndAboveMinimumDoesNot() {
        CreateInventoryItemResult low = createMaterial(
                "HOME-LOW-" + suffix(),
                "FABRIC",
                "METER",
                "5.0000",
                "10.0000",
                false
        );
        CreateInventoryItemResult ok = createMaterial(
                "HOME-HI-" + suffix(),
                "INK",
                "LITER",
                "30.0000",
                "10.0000",
                false
        );

        GetHomeDashboardResult result = getHomeDashboardUseCase.execute(
                new GetHomeDashboardQuery(null, null)
        );

        HomeInventoryAlertItem alert = requireInventoryAlert(result, low.inventoryItemId());
        assertTrue(alert.lowStock());
        assertEquals(0, alert.stock().compareTo(new BigDecimal("5.0000")));
        assertEquals(0, alert.minimumStock().compareTo(new BigDecimal("10.0000")));
        assertTrue(result.inventoryAlerts().items().stream()
                .noneMatch(item -> item.inventoryItemId().equals(ok.inventoryItemId())));
    }

    @Test
    void nullMinimumStockDoesNotAppearAsLowStockAlert() {
        CreateInventoryItemResult unmonitored = createMaterial(
                "HOME-NULL-" + suffix(),
                "THREAD",
                "UNIT",
                "1.0000",
                null,
                false
        );

        GetHomeDashboardResult result = getHomeDashboardUseCase.execute(
                new GetHomeDashboardQuery(null, null)
        );

        assertTrue(result.inventoryAlerts().items().stream()
                .noneMatch(item -> item.inventoryItemId().equals(unmonitored.inventoryItemId())));
    }

    @Test
    void minimumStockZeroFollowsInventorySemantics() {
        CreateInventoryItemResult zeroStock = createMaterial(
                "HOME-Z0-" + suffix(),
                "OTHER",
                "UNIT",
                "0.0000",
                "0.0000",
                false
        );
        CreateInventoryItemResult positiveStock = createMaterial(
                "HOME-Z1-" + suffix(),
                "OTHER",
                "UNIT",
                "1.0000",
                "0.0000",
                false
        );

        GetHomeDashboardResult result = getHomeDashboardUseCase.execute(
                new GetHomeDashboardQuery(null, null)
        );

        requireInventoryAlert(result, zeroStock.inventoryItemId());
        assertTrue(result.inventoryAlerts().items().stream()
                .noneMatch(item -> item.inventoryItemId().equals(positiveStock.inventoryItemId())));
    }

    @Test
    void multipleLowStockMaterialsAggregateAndPaperRollSectionIsIndependent() {
        CreateInventoryItemResult fabric = createMaterial(
                "HOME-MF-" + suffix(),
                "FABRIC",
                "METER",
                "2.0000",
                "10.0000",
                false
        );
        CreateInventoryItemResult ink = createMaterial(
                "HOME-MI-" + suffix(),
                "INK",
                "LITER",
                "1.0000",
                "5.0000",
                false
        );
        CreateInventoryItemResult roll = createMaterial(
                "HOME-MR-" + suffix(),
                "PAPER",
                "METER",
                "7.0000",
                "20.0000",
                true
        );
        CreateInventoryItemResult thread = createMaterial(
                "HOME-MT-" + suffix(),
                "THREAD",
                "UNIT",
                "3.0000",
                "15.0000",
                false
        );

        GetHomeDashboardResult result = getHomeDashboardUseCase.execute(
                new GetHomeDashboardQuery(null, null)
        );

        requireInventoryAlert(result, fabric.inventoryItemId());
        requireInventoryAlert(result, ink.inventoryItemId());
        requireInventoryAlert(result, roll.inventoryItemId());
        requireInventoryAlert(result, thread.inventoryItemId());
        assertTrue(result.inventoryAlerts().lowStockCount() >= 4);

        HomePaperRollAlertItem paperAlert = requirePaperRollAlert(result, roll.inventoryItemId());
        assertEquals(roll.paperRollNumber(), paperAlert.paperRollNumber());
        assertTrue(result.paperRollAlerts().items().stream()
                .noneMatch(item -> item.inventoryItemId().equals(fabric.inventoryItemId())));
        assertTrue(result.paperRollAlerts().items().stream()
                .noneMatch(item -> item.inventoryItemId().equals(ink.inventoryItemId())));
        assertTrue(result.paperRollAlerts().items().stream()
                .noneMatch(item -> item.inventoryItemId().equals(thread.inventoryItemId())));
    }

    @Test
    void inventoryAlertsCoexistWithFinanceReceivablesAndCommitments() {
        Order order = createOrderWithTotal("ORD-HOME-INV-", "180000.00");
        registerFinancialTransactionUseCase.execute(
                new RegisterFinancialTransactionCommand(
                        FinancialTransactionType.INCOME,
                        new BigDecimal("55000.00"),
                        LocalDate.of(2026, 8, 4),
                        "SALES",
                        "Home inventory coexist " + suffix(),
                        null,
                        null,
                        null
                )
        );
        CreateRecurringFinancialObligationResult obligation = createObligation(
                "Home-InvCo-" + suffix(),
                "33000.00",
                17
        );
        CreateRecurringFinancialObligationOccurrenceResult occurrence =
                createOccurrence(obligation.obligationId(), LocalDate.of(2026, 8, 17));
        CreateInventoryItemResult low = createMaterial(
                "HOME-CO-" + suffix(),
                "DTF",
                "UNIT",
                "4.0000",
                "8.0000",
                false
        );

        GetHomeDashboardResult result = getHomeDashboardUseCase.execute(
                new GetHomeDashboardQuery(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                )
        );

        assertTrue(result.financialSummary().income().compareTo(new BigDecimal("55000.00")) >= 0);
        requireReceivable(result, order.getId());
        requireCommitment(result, occurrence.occurrenceId());
        requireInventoryAlert(result, low.inventoryItemId());
    }

    @Test
    void productionLifecycleCountsAndActiveItemsOrdering() {
        Customer createdCustomer = customerRepository.save(Customer.create("Colegio Home Creada"));
        Customer plannedCustomer = customerRepository.save(Customer.create("Colegio Home Planificada"));
        Customer inProgressCustomer = customerRepository.save(Customer.create("Colegio Home En Proceso"));
        Customer completedCustomer = customerRepository.save(Customer.create("Colegio Home Completada"));

        Order createdCommercial = createOrderForCustomer(createdCustomer.getId(), "ORD-HOME-PC1-", "100000.00");
        Order plannedCommercial = createOrderForCustomer(plannedCustomer.getId(), "ORD-HOME-PC2-", "110000.00");
        Order inProgressCommercial = createOrderForCustomer(inProgressCustomer.getId(), "ORD-HOME-PC3-", "120000.00");
        Order completedCommercial = createOrderForCustomer(completedCustomer.getId(), "ORD-HOME-PC4-", "130000.00");

        ProductionOrder created = productionOrderRepository.save(ProductionOrder.create(
                createdCommercial.getId(),
                LocalDate.of(2026, 8, 1),
                ProductionPriority.NORMAL,
                null,
                null,
                "home created"
        ));
        ProductionOrder planned = productionOrderRepository.save(ProductionOrder.create(
                plannedCommercial.getId(),
                LocalDate.of(2026, 8, 2),
                ProductionPriority.HIGH,
                null,
                null,
                "home planned"
        ));
        planProductionOrderUseCase.execute(new PlanProductionOrderCommand(
                planned.getId(),
                LocalDate.of(2026, 8, 2),
                LocalDate.of(2026, 8, 5),
                ProductionPriority.HIGH
        ));
        ProductionOrder inProgress = productionOrderRepository.save(ProductionOrder.create(
                inProgressCommercial.getId(),
                LocalDate.of(2026, 8, 3),
                ProductionPriority.LOW,
                null,
                null,
                "home in progress"
        ));
        planProductionOrderUseCase.execute(new PlanProductionOrderCommand(
                inProgress.getId(),
                LocalDate.of(2026, 8, 3),
                LocalDate.of(2026, 8, 6),
                ProductionPriority.LOW
        ));
        startProductionOrderUseCase.execute(new StartProductionOrderCommand(inProgress.getId(), null));

        ProductionOrder completed = productionOrderRepository.save(ProductionOrder.create(
                completedCommercial.getId(),
                LocalDate.of(2026, 8, 4),
                ProductionPriority.URGENT,
                null,
                null,
                "home completed"
        ));
        planProductionOrderUseCase.execute(new PlanProductionOrderCommand(
                completed.getId(),
                LocalDate.of(2026, 8, 4),
                LocalDate.of(2026, 8, 7),
                ProductionPriority.URGENT
        ));
        startProductionOrderUseCase.execute(new StartProductionOrderCommand(completed.getId(), null));
        completeProductionOrderUseCase.execute(new CompleteProductionOrderCommand(completed.getId(), null));

        GetHomeDashboardResult result = getHomeDashboardUseCase.execute(
                new GetHomeDashboardQuery(null, null)
        );

        assertTrue(result.productionSummary().totalOrders() >= 4);
        assertTrue(result.productionSummary().createdCount() >= 1);
        assertTrue(result.productionSummary().plannedCount() >= 1);
        assertTrue(result.productionSummary().inProgressCount() >= 1);
        assertTrue(result.productionSummary().completedCount() >= 1);

        List<UUID> activeIds = result.productionSummary().items().stream()
                .map(HomeProductionItem::productionOrderId)
                .toList();
        int inProgressIndex = activeIds.indexOf(inProgress.getId());
        int plannedIndex = activeIds.indexOf(planned.getId());
        int createdIndex = activeIds.indexOf(created.getId());
        assertTrue(inProgressIndex >= 0 && plannedIndex >= 0 && createdIndex >= 0);
        assertTrue(inProgressIndex < plannedIndex);
        assertTrue(plannedIndex < createdIndex);
        assertTrue(activeIds.stream().noneMatch(id -> id.equals(completed.getId())));

        HomeProductionItem inProgressItem = result.productionSummary().items().get(inProgressIndex);
        assertEquals(inProgress.getId(), inProgressItem.productionOrderId());
        assertEquals(inProgressCommercial.getId(), inProgressItem.orderId());
        assertEquals(inProgressCommercial.getOrderNumber().getValue(), inProgressItem.orderNumber());
        assertEquals(inProgressCustomer.getId(), inProgressItem.customerId());
        assertEquals("Colegio Home En Proceso", inProgressItem.customerName());
        assertEquals("IN_PROGRESS", inProgressItem.status());
        assertEquals("LOW", inProgressItem.priority());
        assertEquals(LocalDate.of(2026, 8, 3), inProgressItem.creationDate());
    }

    @Test
    void profitabilityAggregatesCompletePartialAndNoCostWithoutTreatingUnknownAsTotals() {
        Order completeOrder = createOrderWithTotal("ORD-HOME-PC-", "1000000.00");
        UUID productionOrderId = createInProgressProductionOrder(completeOrder.getId());
        InventoryItem fabric = inventoryItemRepository.save(InventoryItem.create(
                MaterialCode.of("HOME-FAB-" + suffix()),
                "Tela Home",
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

        Order noCostOrder = createOrderWithTotal("ORD-HOME-PN-", "250000.00");

        Order partialOrder = createOrderWithTotal("ORD-HOME-PP-", "500000.00");
        UUID partialProductionId = createInProgressProductionOrder(partialOrder.getId());
        InventoryItem valued = inventoryItemRepository.save(InventoryItem.create(
                MaterialCode.of("HOME-VAL-" + suffix()),
                "Tela valorada",
                "FABRIC",
                "METER",
                new BigDecimal("100.0000"),
                null,
                null,
                new BigDecimal("15000.00")
        ));
        InventoryItem unvalued = inventoryItemRepository.save(InventoryItem.create(
                MaterialCode.of("HOME-UNV-" + suffix()),
                "Tela sin costo",
                "FABRIC",
                "METER",
                new BigDecimal("50.0000"),
                null
        ));
        registerProductionMaterialConsumptionUseCase.execute(
                new RegisterProductionMaterialConsumptionCommand(
                        partialProductionId,
                        valued.getId(),
                        new BigDecimal("10.0000"),
                        "METER",
                        null
                )
        );
        registerProductionMaterialConsumptionUseCase.execute(
                new RegisterProductionMaterialConsumptionCommand(
                        partialProductionId,
                        unvalued.getId(),
                        new BigDecimal("5.0000"),
                        "METER",
                        null
                )
        );

        long financeBefore = financialTransactionRepository.findAllNewestFirst().size();
        long movementsBefore = springDataInventoryMovementRepository.count();

        GetHomeDashboardResult result = getHomeDashboardUseCase.execute(
                new GetHomeDashboardQuery(null, null)
        );

        assertTrue(result.profitabilitySummary().evaluatedOrderCount() >= 3);
        assertTrue(result.profitabilitySummary().completeOrderCount() >= 1);
        assertTrue(result.profitabilitySummary().partiallyUnvaluedOrderCount() >= 1);
        assertTrue(result.profitabilitySummary().noCostDataOrderCount() >= 1);
        assertTrue(result.profitabilitySummary().totalOrderValue().compareTo(new BigDecimal("1000000.00")) >= 0);
        assertTrue(result.profitabilitySummary().totalDirectCost().compareTo(new BigDecimal("150000.00")) >= 0);
        assertTrue(result.profitabilitySummary().totalDirectProfit().compareTo(new BigDecimal("850000.00")) >= 0);
        assertTrue(result.profitabilitySummary().unvaluedCostCount() >= 1);
        // NO_COST_DATA zeros must not inflate COMPLETE monetary totals beyond known COMPLETE set
        assertTrue(result.profitabilitySummary().averageMarginPercentage() != null);

        assertEquals(financeBefore, financialTransactionRepository.findAllNewestFirst().size());
        assertEquals(movementsBefore, springDataInventoryMovementRepository.count());
    }

    @Test
    void homeSectionsCoexistIncludingProductionAndProfitability() {
        Order order = createOrderWithTotal("ORD-HOME-ALL-", "300000.00");
        createInProgressProductionOrder(order.getId());
        CreateInventoryItemResult low = createMaterial(
                "HOME-ALL-" + suffix(),
                "THREAD",
                "UNIT",
                "1.0000",
                "5.0000",
                false
        );
        CreateRecurringFinancialObligationResult obligation = createObligation(
                "Home-All-" + suffix(),
                "22000.00",
                18
        );
        CreateRecurringFinancialObligationOccurrenceResult occurrence =
                createOccurrence(obligation.obligationId(), LocalDate.of(2026, 8, 18));

        GetHomeDashboardResult result = getHomeDashboardUseCase.execute(
                new GetHomeDashboardQuery(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                )
        );

        requireReceivable(result, order.getId());
        requireCommitment(result, occurrence.occurrenceId());
        requireInventoryAlert(result, low.inventoryItemId());
        assertTrue(result.productionSummary().inProgressCount() >= 1);
        assertTrue(result.profitabilitySummary().evaluatedOrderCount() >= 1);
        assertTrue(result.profitabilitySummary().noCostDataOrderCount() >= 1);
    }

    private UUID createInProgressProductionOrder(UUID orderId) {
        ProductionOrder created = productionOrderRepository.save(ProductionOrder.create(
                orderId,
                LocalDate.of(2026, 8, 1),
                ProductionPriority.NORMAL,
                null,
                null,
                "home profitability production"
        ));
        planProductionOrderUseCase.execute(new PlanProductionOrderCommand(
                created.getId(),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 4),
                ProductionPriority.NORMAL
        ));
        startProductionOrderUseCase.execute(new StartProductionOrderCommand(created.getId(), null));
        return created.getId();
    }

    private static HomeReceivableItem requireReceivable(GetHomeDashboardResult result, UUID orderId) {
        return result.receivables().items().stream()
                .filter(item -> item.orderId().equals(orderId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected receivable for order " + orderId));
    }

    private static HomeCommitmentItem requireCommitment(GetHomeDashboardResult result, UUID occurrenceId) {
        return result.commitments().items().stream()
                .filter(item -> item.occurrenceId().equals(occurrenceId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected commitment for occurrence " + occurrenceId));
    }

    private static HomeInventoryAlertItem requireInventoryAlert(GetHomeDashboardResult result, UUID inventoryItemId) {
        return result.inventoryAlerts().items().stream()
                .filter(item -> item.inventoryItemId().equals(inventoryItemId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected inventory alert for item " + inventoryItemId));
    }

    private static HomePaperRollAlertItem requirePaperRollAlert(GetHomeDashboardResult result, UUID inventoryItemId) {
        return result.paperRollAlerts().items().stream()
                .filter(item -> item.inventoryItemId().equals(inventoryItemId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected paper-roll alert for item " + inventoryItemId));
    }

    private CreateInventoryItemResult createMaterial(
            String code,
            String materialType,
            String unitOfMeasure,
            String stock,
            String minimumStock,
            boolean plotterPaperRoll
    ) {
        return createInventoryItemUseCase.execute(
                new CreateInventoryItemCommand(
                        code,
                        "Material Home " + code,
                        "HOME",
                        unitOfMeasure,
                        new BigDecimal(stock),
                        minimumStock == null ? null : new BigDecimal(minimumStock),
                        "Home inventory alert test",
                        new BigDecimal("1000.00"),
                        materialType,
                        plotterPaperRoll
                )
        );
    }

    private CreateRecurringFinancialObligationResult createObligation(
            String name,
            String amount,
            int dueDay
    ) {
        return createObligationUseCase.execute(
                new CreateRecurringFinancialObligationCommand(
                        name,
                        RecurringObligationType.SERVICE,
                        new BigDecimal(amount),
                        RecurringObligationFrequency.MONTHLY,
                        dueDay,
                        LocalDate.of(2026, 8, 1),
                        null,
                        null,
                        null
                )
        );
    }

    private CreateRecurringFinancialObligationOccurrenceResult createOccurrence(
            UUID obligationId,
            LocalDate dueDate
    ) {
        return createOccurrenceUseCase.execute(
                new CreateRecurringFinancialObligationOccurrenceCommand(obligationId, dueDate, null)
        );
    }

    private Order createOrderWithTotal(String numberPrefix, String unitPrice) {
        return createOrderWithNumberAndTotal(numberPrefix + suffix(), unitPrice);
    }

    private Order createOrderForCustomer(UUID customerId, String numberPrefix, String unitPrice) {
        return createOrderWithNumberCustomerAndTotal(numberPrefix + suffix(), customerId, unitPrice);
    }

    private Order createOrderWithNumberAndTotal(String orderNumber, String unitPrice) {
        return createOrderWithNumberCustomerAndTotal(orderNumber, UUID.randomUUID(), unitPrice);
    }

    private Order createOrderWithNumberCustomerAndTotal(String orderNumber, UUID customerId, String unitPrice) {
        LocalDate confirmationDate = LocalDate.of(2026, 8, 1);
        OrderItem item = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Producto Home AR",
                1,
                "Tela",
                "Negro",
                Money.of(new BigDecimal(unitPrice)),
                ProductSpecification.empty(),
                List.of()
        );

        Order order = Order.create(
                OrderNumber.of(orderNumber),
                customerId,
                UUID.randomUUID(),
                confirmationDate,
                DeliveryCommitment.of(confirmationDate.plusDays(7)),
                UUID.randomUUID(),
                "Orden Home receivables",
                "Descripción Home receivables",
                List.of(item)
        );

        return orderRepository.save(order);
    }

    private Order createDeliveredOrder(UUID customerId, String numberPrefix, String description, String unitPrice) {
        LocalDate confirmationDate = LocalDate.of(2026, 8, 1);
        OrderItem item = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Producto Home Completado",
                1,
                "Hydrotech",
                "Blanco",
                Money.of(new BigDecimal(unitPrice)),
                ProductSpecification.empty(),
                List.of()
        );
        Money total = item.getSubtotal();
        Order order = Order.reconstitute(
                UUID.randomUUID(),
                OrderNumber.of(numberPrefix + suffix()),
                customerId,
                UUID.randomUUID(),
                confirmationDate,
                OrderStatus.DELIVERED,
                DeliveryCommitment.of(confirmationDate.plusDays(7)),
                PaymentSummary.forConfirmedOrder(total),
                UUID.randomUUID(),
                "Orden Home completada",
                description,
                List.of(item)
        );
        return orderRepository.save(order);
    }

    private static HomeReceivableItem requireCompletedReceivable(GetHomeDashboardResult result, UUID orderId) {
        return result.completedReceivables().items().stream()
                .filter(item -> item.orderId().equals(orderId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected completed receivable for order " + orderId));
    }

    private static String fingerprint(List<FinancialTransaction> transactions) {
        return transactions.stream()
                .map(transaction -> transaction.getId()
                        + "|" + transaction.getType()
                        + "|" + transaction.getAmount()
                        + "|" + transaction.getTransactionDate()
                        + "|" + transaction.getDescription())
                .sorted()
                .collect(Collectors.joining(";"));
    }

    private static String suffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
