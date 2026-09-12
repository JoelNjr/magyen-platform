package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityByPromisedDeliveryPeriodQuery;
import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityQuery;
import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityResult;
import com.magyen.platform.commercial.application.dto.OrderProfitabilitySummary;
import com.magyen.platform.commercial.domain.DeliveryCommitment;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderItem;
import com.magyen.platform.commercial.domain.OrderNumber;
import com.magyen.platform.commercial.domain.OrderProfitabilityStatus;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.OrderStatus;
import com.magyen.platform.commercial.domain.PaymentSummary;
import com.magyen.platform.commercial.domain.ProductSpecification;
import com.magyen.platform.commercial.domain.exception.OrderDomainException;
import com.magyen.platform.shared.domain.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetOrderProfitabilityByPromisedDeliveryPeriodUseCaseTest {

    private static final LocalDate SEPTEMBER_START = LocalDate.of(2026, 9, 1);
    private static final LocalDate SEPTEMBER_END = LocalDate.of(2026, 9, 30);

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private GetOrderProfitabilityUseCase getOrderProfitabilityUseCase;

    @Test
    void queriesRepositoryWithInclusivePromisedDeliveryRange() {
        when(orderRepository.findByPromisedDeliveryDateBetween(SEPTEMBER_START, SEPTEMBER_END))
                .thenReturn(List.of());

        new GetOrderProfitabilityByPromisedDeliveryPeriodUseCase(
                orderRepository,
                getOrderProfitabilityUseCase
        ).execute(new GetOrderProfitabilityByPromisedDeliveryPeriodQuery(SEPTEMBER_START, SEPTEMBER_END));

        verify(orderRepository).findByPromisedDeliveryDateBetween(SEPTEMBER_START, SEPTEMBER_END);
        verify(getOrderProfitabilityUseCase, never()).execute(any());
    }

    @Test
    void includesEligibleStatusesAndExcludesClosed() {
        Order confirmed = order(OrderStatus.CONFIRMED, LocalDate.of(2026, 8, 20), LocalDate.of(2026, 9, 3));
        Order inProduction = order(OrderStatus.IN_PRODUCTION, LocalDate.of(2026, 8, 21), LocalDate.of(2026, 9, 10));
        Order ready = order(OrderStatus.READY_FOR_DELIVERY, LocalDate.of(2026, 8, 22), LocalDate.of(2026, 9, 15));
        Order delivered = order(OrderStatus.DELIVERED, LocalDate.of(2026, 8, 23), LocalDate.of(2026, 9, 20));
        Order closed = order(OrderStatus.CLOSED, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 9, 25));

        when(orderRepository.findByPromisedDeliveryDateBetween(SEPTEMBER_START, SEPTEMBER_END))
                .thenReturn(List.of(confirmed, inProduction, ready, delivered, closed));
        when(getOrderProfitabilityUseCase.execute(any())).thenAnswer(invocation -> {
            UUID orderId = invocation.getArgument(0, GetOrderProfitabilityQuery.class).orderId();
            return noCost(orderId);
        });

        OrderProfitabilitySummary summary = new GetOrderProfitabilityByPromisedDeliveryPeriodUseCase(
                orderRepository,
                getOrderProfitabilityUseCase
        ).execute(new GetOrderProfitabilityByPromisedDeliveryPeriodQuery(SEPTEMBER_START, SEPTEMBER_END));

        assertEquals(4, summary.evaluatedOrderCount());
        assertEquals(4, summary.noCostDataOrderCount());

        ArgumentCaptor<GetOrderProfitabilityQuery> captor = ArgumentCaptor.forClass(GetOrderProfitabilityQuery.class);
        verify(getOrderProfitabilityUseCase, org.mockito.Mockito.times(4)).execute(captor.capture());
        List<UUID> evaluatedIds = captor.getAllValues().stream().map(GetOrderProfitabilityQuery::orderId).toList();
        assertTrue(evaluatedIds.containsAll(List.of(
                confirmed.getId(),
                inProduction.getId(),
                ready.getId(),
                delivered.getId()
        )));
        assertTrue(evaluatedIds.stream().noneMatch(id -> id.equals(closed.getId())));
    }

    @Test
    void rejectsIncompletePeriod() {
        GetOrderProfitabilityByPromisedDeliveryPeriodUseCase useCase =
                new GetOrderProfitabilityByPromisedDeliveryPeriodUseCase(
                        orderRepository,
                        getOrderProfitabilityUseCase
                );

        assertThrows(
                OrderDomainException.class,
                () -> useCase.execute(new GetOrderProfitabilityByPromisedDeliveryPeriodQuery(SEPTEMBER_START, null))
        );
        assertThrows(
                OrderDomainException.class,
                () -> useCase.execute(new GetOrderProfitabilityByPromisedDeliveryPeriodQuery(
                        LocalDate.of(2026, 10, 1),
                        SEPTEMBER_END
                ))
        );
    }

    private static Order order(OrderStatus status, LocalDate confirmationDate, LocalDate promisedDeliveryDate) {
        OrderItem item = OrderItem.reconstitute(
                UUID.randomUUID(),
                "Camiseta",
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
        return Order.reconstitute(
                UUID.randomUUID(),
                OrderNumber.of("ORD-PD-" + UUID.randomUUID().toString().substring(0, 8)),
                UUID.randomUUID(),
                UUID.randomUUID(),
                confirmationDate,
                status,
                DeliveryCommitment.of(promisedDeliveryDate),
                payment,
                UUID.randomUUID(),
                null,
                "Pedido período Home",
                List.of(item)
        );
    }

    private static GetOrderProfitabilityResult noCost(UUID orderId) {
        return new GetOrderProfitabilityResult(
                orderId,
                new BigDecimal("100000.00"),
                BigDecimal.ZERO,
                new BigDecimal("100000.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO,
                new BigDecimal("100000.00"),
                new BigDecimal("100.00"),
                0,
                OrderProfitabilityStatus.NO_COST_DATA
        );
    }
}
