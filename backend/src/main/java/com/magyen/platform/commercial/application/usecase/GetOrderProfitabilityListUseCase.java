package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.OrderProfitabilityAggregator;
import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityListQuery;
import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityListResult;
import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityQuery;
import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityResult;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderIndividualProfitabilityMonthPolicy;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.exception.OrderDomainException;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Lista la rentabilidad individual de las órdenes elegibles del mes seleccionado.
 * <p>
 * Reusa {@link GetOrderProfitabilityUseCase}; no duplica la fórmula.
 * El mes clasifica por entrega real (o fallback histórico); no usa el período de Home.
 */
public class GetOrderProfitabilityListUseCase {

    private static final Comparator<GetOrderProfitabilityResult> ORDER =
            Comparator.comparing(GetOrderProfitabilityResult::actualDeliveryDate,
                            Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(GetOrderProfitabilityResult::promisedDeliveryDate,
                            Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(GetOrderProfitabilityResult::orderNumber,
                            Comparator.nullsLast(String::compareTo))
                    .thenComparing(item -> item.orderId().toString());

    private final OrderRepository orderRepository;
    private final GetOrderProfitabilityUseCase getOrderProfitabilityUseCase;
    private final Clock clock;

    public GetOrderProfitabilityListUseCase(
            OrderRepository orderRepository,
            GetOrderProfitabilityUseCase getOrderProfitabilityUseCase,
            Clock clock
    ) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "Order repository must not be null");
        this.getOrderProfitabilityUseCase = Objects.requireNonNull(
                getOrderProfitabilityUseCase,
                "Get order profitability use case must not be null"
        );
        this.clock = Objects.requireNonNull(clock, "Clock must not be null");
    }

    public GetOrderProfitabilityListResult execute() {
        return execute(new GetOrderProfitabilityListQuery(null, null));
    }

    public GetOrderProfitabilityListResult execute(GetOrderProfitabilityListQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        ResolvedPeriod period = resolvePeriod(query.fromDate(), query.toDate());
        LocalDate today = LocalDate.now(clock);
        boolean includeUndelivered = OrderIndividualProfitabilityMonthPolicy.isCurrentCalendarMonth(
                period.fromDate(),
                period.toDate(),
                today
        );

        List<GetOrderProfitabilityResult> orders = orderRepository.findForIndividualProfitabilityMonth(
                        period.fromDate(),
                        period.toDate(),
                        includeUndelivered
                ).stream()
                .filter(order -> OrderIndividualProfitabilityMonthPolicy.includes(
                        order,
                        period.fromDate(),
                        period.toDate(),
                        today
                ))
                .map(this::toProfitability)
                .sorted(ORDER)
                .toList();

        return new GetOrderProfitabilityListResult(
                List.copyOf(orders),
                OrderProfitabilityAggregator.summarize(orders),
                period.fromDate(),
                period.toDate()
        );
    }

    private GetOrderProfitabilityResult toProfitability(Order order) {
        return getOrderProfitabilityUseCase.execute(new GetOrderProfitabilityQuery(order.getId()));
    }

    private ResolvedPeriod resolvePeriod(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null && toDate == null) {
            LocalDate today = LocalDate.now(clock);
            return new ResolvedPeriod(
                    today.withDayOfMonth(1),
                    today.withDayOfMonth(today.lengthOfMonth())
            );
        }
        if (fromDate == null || toDate == null) {
            throw new OrderDomainException("Both fromDate and toDate must be provided together");
        }
        if (fromDate.isAfter(toDate)) {
            throw new OrderDomainException("From date must not be after to date");
        }
        return new ResolvedPeriod(fromDate, toDate);
    }

    private record ResolvedPeriod(LocalDate fromDate, LocalDate toDate) {
    }
}
