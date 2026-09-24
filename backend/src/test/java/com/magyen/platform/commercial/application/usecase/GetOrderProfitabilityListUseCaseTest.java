package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityByPromisedDeliveryPeriodQuery;
import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityListQuery;
import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityListResult;
import com.magyen.platform.commercial.application.dto.OrderProfitabilitySummary;
import com.magyen.platform.commercial.domain.DeliveryCommitment;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderItem;
import com.magyen.platform.commercial.domain.OrderNumber;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.OrderStatus;
import com.magyen.platform.commercial.domain.PaymentSummary;
import com.magyen.platform.commercial.domain.ProductSpecification;
import com.magyen.platform.commercial.domain.exception.OrderDomainException;
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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class GetOrderProfitabilityListUseCaseTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 14);
    private static final LocalDate SEPTEMBER_START = LocalDate.of(2026, 9, 1);
    private static final LocalDate SEPTEMBER_END = LocalDate.of(2026, 9, 30);
    private static final LocalDate AUGUST_START = LocalDate.of(2026, 8, 1);
    private static final LocalDate AUGUST_END = LocalDate.of(2026, 8, 31);
    private static final LocalDate OCTOBER_START = LocalDate.of(2026, 10, 1);
    private static final LocalDate OCTOBER_END = LocalDate.of(2026, 10, 31);

    @MockitoBean
    private Clock clock;

    @Autowired
    private GetOrderProfitabilityListUseCase getOrderProfitabilityListUseCase;

    @Autowired
    private GetOrderProfitabilityByPromisedDeliveryPeriodUseCase
            getOrderProfitabilityByPromisedDeliveryPeriodUseCase;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setFixedClock() {
        ZoneId zone = ZoneId.systemDefault();
        when(clock.getZone()).thenReturn(zone);
        when(clock.instant()).thenReturn(TODAY.atStartOfDay(zone).toInstant());
    }

    @Test
    void omittedDatesDefaultToCurrentCalendarMonth() {
        GetOrderProfitabilityListResult result = getOrderProfitabilityListUseCase.execute();
        assertEquals(SEPTEMBER_START, result.fromDate());
        assertEquals(SEPTEMBER_END, result.toDate());
    }

    @Test
    void rejectsIncompletePeriod() {
        assertThrows(
                OrderDomainException.class,
                () -> getOrderProfitabilityListUseCase.execute(
                        new GetOrderProfitabilityListQuery(SEPTEMBER_START, null)
                )
        );
    }

    @Test
    void classifiesDeliveredActualHistoricalFallbackAndCurrentWip() {
        Order deliveredInSeptember = saveDelivered(
                LocalDate.of(2026, 9, 18),
                LocalDate.of(2026, 8, 4)
        );
        Order deliveredInOctober = saveDelivered(
                LocalDate.of(2026, 10, 2),
                LocalDate.of(2026, 9, 14)
        );
        Order historicalFallback = saveDelivered(null, LocalDate.of(2026, 9, 20));
        Order historicalFallbackAugust = saveDelivered(null, LocalDate.of(2026, 8, 15));
        Order confirmed = saveStatus(OrderStatus.CONFIRMED, LocalDate.of(2026, 9, 25));
        Order inProduction = saveStatus(OrderStatus.IN_PRODUCTION, LocalDate.of(2026, 9, 25));
        Order ready = saveStatus(OrderStatus.READY_FOR_DELIVERY, LocalDate.of(2026, 9, 25));
        Order closed = saveStatus(OrderStatus.CLOSED, LocalDate.of(2026, 9, 25));

        Set<UUID> september = ids(getOrderProfitabilityListUseCase.execute(
                new GetOrderProfitabilityListQuery(SEPTEMBER_START, SEPTEMBER_END)
        ));
        assertTrue(september.contains(deliveredInSeptember.getId()));
        assertTrue(september.contains(historicalFallback.getId()));
        assertTrue(september.contains(confirmed.getId()));
        assertTrue(september.contains(inProduction.getId()));
        assertTrue(september.contains(ready.getId()));
        assertFalse(september.contains(deliveredInOctober.getId()));
        assertFalse(september.contains(historicalFallbackAugust.getId()));
        assertFalse(september.contains(closed.getId()));

        Set<UUID> august = ids(getOrderProfitabilityListUseCase.execute(
                new GetOrderProfitabilityListQuery(AUGUST_START, AUGUST_END)
        ));
        assertTrue(august.contains(historicalFallbackAugust.getId()));
        assertFalse(august.contains(confirmed.getId()));
        assertFalse(august.contains(inProduction.getId()));
        assertFalse(august.contains(ready.getId()));
        assertFalse(august.contains(deliveredInSeptember.getId()));

        Set<UUID> october = ids(getOrderProfitabilityListUseCase.execute(
                new GetOrderProfitabilityListQuery(OCTOBER_START, OCTOBER_END)
        ));
        assertTrue(october.contains(deliveredInOctober.getId()));
        assertFalse(october.contains(confirmed.getId()));
        assertFalse(october.contains(historicalFallback.getId()));
    }

    @Test
    void homePromisedDeliveryProfitabilityRemainsUnchanged() {
        Order promisedSeptemberActualOctober = saveDelivered(
                LocalDate.of(2026, 10, 2),
                LocalDate.of(2026, 9, 14)
        );

        OrderProfitabilitySummary homeSeptember = getOrderProfitabilityByPromisedDeliveryPeriodUseCase.execute(
                new GetOrderProfitabilityByPromisedDeliveryPeriodQuery(SEPTEMBER_START, SEPTEMBER_END)
        );
        Set<UUID> individualSeptember = ids(getOrderProfitabilityListUseCase.execute(
                new GetOrderProfitabilityListQuery(SEPTEMBER_START, SEPTEMBER_END)
        ));

        assertTrue(homeSeptember.evaluatedOrderCount() >= 1);
        assertFalse(individualSeptember.contains(promisedSeptemberActualOctober.getId()));
    }

    private static Set<UUID> ids(GetOrderProfitabilityListResult result) {
        return result.orders().stream().map(item -> item.orderId()).collect(Collectors.toSet());
    }

    private Order saveDelivered(LocalDate actualDeliveryDate, LocalDate promisedDeliveryDate) {
        return save(OrderStatus.DELIVERED, promisedDeliveryDate, actualDeliveryDate);
    }

    private Order saveStatus(OrderStatus status, LocalDate promisedDeliveryDate) {
        return save(status, promisedDeliveryDate, null);
    }

    private Order save(OrderStatus status, LocalDate promisedDeliveryDate, LocalDate actualDeliveryDate) {
        LocalDate confirmationDate = LocalDate.of(2026, 7, 1);
        OrderItem item = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Camiseta rentabilidad mensual",
                1,
                "Algodón",
                "Blanco",
                Money.of(new BigDecimal("100000.00")),
                ProductSpecification.empty(),
                List.of()
        );
        Money total = item.getSubtotal();
        PaymentSummary payment = status == OrderStatus.CLOSED
                ? PaymentSummary.of(true, true, total)
                : PaymentSummary.forConfirmedOrder(total);
        return orderRepository.save(Order.reconstitute(
                UUID.randomUUID(),
                OrderNumber.of("ORD-IPM-" + UUID.randomUUID().toString().substring(0, 8)),
                UUID.randomUUID(),
                UUID.randomUUID(),
                confirmationDate,
                status,
                DeliveryCommitment.of(promisedDeliveryDate),
                payment,
                UUID.randomUUID(),
                null,
                "Individual profitability month",
                List.of(item),
                Money.zero(),
                actualDeliveryDate
        ));
    }
}
