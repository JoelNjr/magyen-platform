package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.OrderProfitabilityAggregator;
import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityListResult;
import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityQuery;
import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityResult;
import com.magyen.platform.commercial.application.dto.OrderResult;
import com.magyen.platform.commercial.domain.OrderProfitabilityEligibility;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Lista la rentabilidad individual de las órdenes elegibles.
 * <p>
 * Reusa {@link GetOrderProfitabilityUseCase}; no duplica la fórmula.
 * El resumen ponderado es el mismo que Home.
 */
public class GetOrderProfitabilityListUseCase {

    private static final Comparator<GetOrderProfitabilityResult> ORDER =
            Comparator.comparing(GetOrderProfitabilityResult::promisedDeliveryDate,
                            Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(GetOrderProfitabilityResult::orderNumber,
                            Comparator.nullsLast(String::compareTo))
                    .thenComparing(item -> item.orderId().toString());

    private final GetOrdersUseCase getOrdersUseCase;
    private final GetOrderProfitabilityUseCase getOrderProfitabilityUseCase;

    public GetOrderProfitabilityListUseCase(
            GetOrdersUseCase getOrdersUseCase,
            GetOrderProfitabilityUseCase getOrderProfitabilityUseCase
    ) {
        this.getOrdersUseCase = Objects.requireNonNull(getOrdersUseCase, "Get orders use case must not be null");
        this.getOrderProfitabilityUseCase = Objects.requireNonNull(
                getOrderProfitabilityUseCase,
                "Get order profitability use case must not be null"
        );
    }

    public GetOrderProfitabilityListResult execute() {
        List<GetOrderProfitabilityResult> orders = getOrdersUseCase.execute().orders().stream()
                .filter(order -> OrderProfitabilityEligibility.includes(order.status()))
                .map(this::toProfitability)
                .sorted(ORDER)
                .toList();

        return new GetOrderProfitabilityListResult(
                List.copyOf(orders),
                OrderProfitabilityAggregator.summarize(orders)
        );
    }

    private GetOrderProfitabilityResult toProfitability(OrderResult order) {
        return getOrderProfitabilityUseCase.execute(new GetOrderProfitabilityQuery(order.orderId()));
    }
}
