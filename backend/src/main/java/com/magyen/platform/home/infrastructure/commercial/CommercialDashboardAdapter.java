package com.magyen.platform.home.infrastructure.commercial;

import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityByPromisedDeliveryPeriodQuery;
import com.magyen.platform.commercial.application.dto.OrderProfitabilitySummary;
import com.magyen.platform.commercial.application.dto.OrderResult;
import com.magyen.platform.commercial.application.port.OrderPaymentCollectionPort;
import com.magyen.platform.commercial.application.usecase.GetOrderProfitabilityByPromisedDeliveryPeriodUseCase;
import com.magyen.platform.commercial.application.usecase.GetOrdersUseCase;
import com.magyen.platform.commercial.domain.OrderStatus;
import com.magyen.platform.home.application.port.CommercialDashboardPort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Adaptador Home → Commercial/Finance para cuentas por cobrar y rentabilidad.
 * <p>
 * Cobranza: Payments vía {@link OrderPaymentCollectionPort}.
 * Rentabilidad: {@link GetOrderProfitabilityByPromisedDeliveryPeriodUseCase}
 * por entrega programada del período — no se recalculan fórmulas.
 * <p>
 * Órdenes elegibles para rentabilidad: CONFIRMED, IN_PRODUCTION, READY_FOR_DELIVERY, DELIVERED
 * (excluye CLOSED). No existe DRAFT en el dominio de Order.
 */
public class CommercialDashboardAdapter implements CommercialDashboardPort {

    private static final Set<OrderStatus> COMPLETED_RECEIVABLE_STATUSES = EnumSet.of(
            OrderStatus.DELIVERED,
            OrderStatus.CLOSED
    );

    private static final Comparator<ReceivableItem> RECEIVABLE_ORDER = Comparator
            .comparing(ReceivableItem::outstandingAmount, Comparator.reverseOrder())
            .thenComparing(ReceivableItem::promisedDeliveryDate, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(ReceivableItem::orderNumber, Comparator.nullsLast(String::compareTo))
            .thenComparing(item -> item.orderId().toString());

    private final GetOrdersUseCase getOrdersUseCase;
    private final OrderPaymentCollectionPort orderPaymentCollectionPort;
    private final GetOrderProfitabilityByPromisedDeliveryPeriodUseCase
            getOrderProfitabilityByPromisedDeliveryPeriodUseCase;

    public CommercialDashboardAdapter(
            GetOrdersUseCase getOrdersUseCase,
            OrderPaymentCollectionPort orderPaymentCollectionPort,
            GetOrderProfitabilityByPromisedDeliveryPeriodUseCase
                    getOrderProfitabilityByPromisedDeliveryPeriodUseCase
    ) {
        this.getOrdersUseCase = Objects.requireNonNull(getOrdersUseCase, "Get orders use case must not be null");
        this.orderPaymentCollectionPort = Objects.requireNonNull(
                orderPaymentCollectionPort,
                "Order payment collection port must not be null"
        );
        this.getOrderProfitabilityByPromisedDeliveryPeriodUseCase = Objects.requireNonNull(
                getOrderProfitabilityByPromisedDeliveryPeriodUseCase,
                "Promised delivery profitability use case must not be null"
        );
    }

    @Override
    public HomeReceivablesSnapshot getCurrentOutstandingReceivables() {
        return collectOutstandingReceivables(order -> true);
    }

    @Override
    public HomeReceivablesSnapshot getCompletedOutstandingReceivables() {
        return collectOutstandingReceivables(order -> order.status() != null
                && COMPLETED_RECEIVABLE_STATUSES.contains(order.status()));
    }

    private HomeReceivablesSnapshot collectOutstandingReceivables(
            java.util.function.Predicate<OrderResult> orderFilter
    ) {
        List<ReceivableItem> outstandingItems = getOrdersUseCase.execute().orders().stream()
                .filter(orderFilter)
                .map(this::toReceivableItemIfOutstanding)
                .flatMap(Optional::stream)
                .sorted(RECEIVABLE_ORDER)
                .toList();

        BigDecimal totalOutstanding = outstandingItems.stream()
                .map(ReceivableItem::outstandingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCollected = outstandingItems.stream()
                .map(ReceivableItem::collectedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new HomeReceivablesSnapshot(
                totalOutstanding,
                totalCollected,
                outstandingItems.size(),
                outstandingItems
        );
    }

    @Override
    public HomeProfitabilitySummarySnapshot getProfitabilitySummary(LocalDate fromDate, LocalDate toDate) {
        OrderProfitabilitySummary summary = getOrderProfitabilityByPromisedDeliveryPeriodUseCase.execute(
                new GetOrderProfitabilityByPromisedDeliveryPeriodQuery(fromDate, toDate)
        );
        return new HomeProfitabilitySummarySnapshot(
                summary.evaluatedOrderCount(),
                summary.completeOrderCount(),
                summary.partiallyUnvaluedOrderCount(),
                summary.noCostDataOrderCount(),
                summary.totalOrderValue(),
                summary.totalDirectCost(),
                summary.totalDirectProfit(),
                summary.weightedMarginPercentage(),
                summary.unvaluedCostCount()
        );
    }

    private Optional<ReceivableItem> toReceivableItemIfOutstanding(OrderResult order) {
        OrderPaymentCollectionPort.OrderPaymentCollection collection =
                orderPaymentCollectionPort.getCollection(order.orderId());

        BigDecimal outstandingAmount = collection.outstandingAmount();
        if (outstandingAmount == null || outstandingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }

        return Optional.of(new ReceivableItem(
                order.orderId(),
                order.orderNumber(),
                order.description(),
                order.customerId(),
                order.customerName(),
                order.totalAmount(),
                collection.collectedAmount(),
                outstandingAmount,
                order.promisedDeliveryDate()
        ));
    }
}
