package com.magyen.platform.home.application.usecase;

import com.magyen.platform.commercial.domain.DeliveryCommitment;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderItem;
import com.magyen.platform.commercial.domain.OrderNumber;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.OrderStatus;
import com.magyen.platform.commercial.domain.PaymentSummary;
import com.magyen.platform.commercial.domain.ProductSpecification;
import com.magyen.platform.finance.application.dto.RegisterFinancialTransactionCommand;
import com.magyen.platform.finance.application.usecase.RegisterFinancialTransactionUseCase;
import com.magyen.platform.finance.domain.FinancialTransactionType;
import com.magyen.platform.home.application.dto.GetHomeDashboardQuery;
import com.magyen.platform.home.application.dto.GetHomeDashboardResult;
import com.magyen.platform.home.application.dto.GetHomeProfitabilityQuery;
import com.magyen.platform.home.application.dto.GetHomeProfitabilityResult;
import com.magyen.platform.home.application.dto.HomeProfitabilitySummary;
import com.magyen.platform.home.domain.exception.HomeDomainException;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class GetHomeProfitabilityUseCaseTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 13);
    private static final LocalDate SEPTEMBER_START = LocalDate.of(2026, 9, 1);
    private static final LocalDate SEPTEMBER_END = LocalDate.of(2026, 9, 30);
    private static final LocalDate AUGUST_START = LocalDate.of(2026, 8, 1);
    private static final LocalDate AUGUST_END = LocalDate.of(2026, 8, 31);
    private static final LocalDate OCTOBER_START = LocalDate.of(2026, 10, 1);
    private static final LocalDate OCTOBER_END = LocalDate.of(2026, 10, 31);

    @MockitoBean
    private Clock clock;

    @Autowired
    private GetHomeProfitabilityUseCase getHomeProfitabilityUseCase;

    @Autowired
    private GetHomeDashboardUseCase getHomeDashboardUseCase;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RegisterFinancialTransactionUseCase registerFinancialTransactionUseCase;

    @BeforeEach
    void setFixedClock() {
        ZoneId zone = ZoneId.systemDefault();
        when(clock.getZone()).thenReturn(zone);
        when(clock.instant()).thenReturn(TODAY.atStartOfDay(zone).toInstant());
    }

    @Test
    void currentMonthUsesInclusiveCalendarBounds() {
        GetHomeProfitabilityResult result = getHomeProfitabilityUseCase.execute(
                new GetHomeProfitabilityQuery(null, null)
        );

        assertEquals(SEPTEMBER_START, result.fromDate());
        assertEquals(SEPTEMBER_END, result.toDate());
    }

    @Test
    void previousAndNextMonthsUseInclusiveCalendarBounds() {
        GetHomeProfitabilityResult previous = getHomeProfitabilityUseCase.execute(
                new GetHomeProfitabilityQuery(AUGUST_START, AUGUST_END)
        );
        GetHomeProfitabilityResult next = getHomeProfitabilityUseCase.execute(
                new GetHomeProfitabilityQuery(OCTOBER_START, OCTOBER_END)
        );

        assertEquals(AUGUST_START, previous.fromDate());
        assertEquals(AUGUST_END, previous.toDate());
        assertEquals(OCTOBER_START, next.fromDate());
        assertEquals(OCTOBER_END, next.toDate());
    }

    @Test
    void profitabilityUsesPromisedDeliveryAndExcludesClosed() {
        HomeProfitabilitySummary before = profitability(SEPTEMBER_START, SEPTEMBER_END);

        saveOrder(OrderStatus.CONFIRMED, LocalDate.of(2026, 8, 10), LocalDate.of(2026, 9, 1), "100000.00");
        saveOrder(OrderStatus.CONFIRMED, LocalDate.of(2026, 8, 10), LocalDate.of(2026, 9, 30), "110000.00");
        saveOrder(OrderStatus.CONFIRMED, LocalDate.of(2026, 8, 10), LocalDate.of(2026, 10, 1), "120000.00");
        saveOrder(OrderStatus.CLOSED, LocalDate.of(2026, 8, 10), LocalDate.of(2026, 9, 15), "130000.00");

        HomeProfitabilitySummary after = profitability(SEPTEMBER_START, SEPTEMBER_END);

        assertEquals(before.evaluatedOrderCount() + 2, after.evaluatedOrderCount());
        assertEquals(before.noCostDataOrderCount() + 2, after.noCostDataOrderCount());
    }

    @Test
    void profitabilityMonthDoesNotChangeFinanceSnapshotOnDashboard() {
        registerFinancialTransactionUseCase.execute(
                new RegisterFinancialTransactionCommand(
                        FinancialTransactionType.EXPENSE,
                        new BigDecimal("250000.00"),
                        LocalDate.of(2026, 8, 8),
                        "SERVICES",
                        "Home finance isolation " + UUID.randomUUID().toString().substring(0, 8),
                        null,
                        null,
                        null
                )
        );

        GetHomeDashboardResult augustDashboard = getHomeDashboardUseCase.execute(
                new GetHomeDashboardQuery(AUGUST_START, AUGUST_END)
        );
        BigDecimal augustExpense = augustDashboard.financialSummary().expense();
        long augustTransactions = augustDashboard.financialSummary().transactionCount();

        getHomeProfitabilityUseCase.execute(new GetHomeProfitabilityQuery(SEPTEMBER_START, SEPTEMBER_END));
        getHomeProfitabilityUseCase.execute(new GetHomeProfitabilityQuery(OCTOBER_START, OCTOBER_END));

        GetHomeDashboardResult augustAfter = getHomeDashboardUseCase.execute(
                new GetHomeDashboardQuery(AUGUST_START, AUGUST_END)
        );

        assertEquals(0, augustExpense.compareTo(augustAfter.financialSummary().expense()));
        assertEquals(augustTransactions, augustAfter.financialSummary().transactionCount());
        assertEquals(
                augustDashboard.productionSummary().totalOrders(),
                augustAfter.productionSummary().totalOrders()
        );
        assertEquals(
                augustDashboard.receivables().orderCount(),
                augustAfter.receivables().orderCount()
        );
    }

    @Test
    void invalidPeriodIsRejected() {
        assertThrows(
                HomeDomainException.class,
                () -> getHomeProfitabilityUseCase.execute(
                        new GetHomeProfitabilityQuery(SEPTEMBER_END, SEPTEMBER_START)
                )
        );
        assertThrows(
                HomeDomainException.class,
                () -> getHomeProfitabilityUseCase.execute(
                        new GetHomeProfitabilityQuery(SEPTEMBER_START, null)
                )
        );
    }

    private HomeProfitabilitySummary profitability(LocalDate fromDate, LocalDate toDate) {
        return getHomeProfitabilityUseCase.execute(new GetHomeProfitabilityQuery(fromDate, toDate))
                .profitabilitySummary();
    }

    private Order saveOrder(
            OrderStatus status,
            LocalDate confirmationDate,
            LocalDate promisedDeliveryDate,
            String unitPrice
    ) {
        OrderItem item = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Camiseta Home rentabilidad",
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
                OrderNumber.of("ORD-HPR-" + UUID.randomUUID().toString().substring(0, 8)),
                UUID.randomUUID(),
                UUID.randomUUID(),
                confirmationDate,
                status,
                DeliveryCommitment.of(promisedDeliveryDate),
                payment,
                UUID.randomUUID(),
                null,
                "Home profitability period",
                List.of(item)
        ));
    }
}
