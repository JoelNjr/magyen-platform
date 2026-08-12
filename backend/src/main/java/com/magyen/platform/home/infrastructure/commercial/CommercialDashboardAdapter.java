package com.magyen.platform.home.infrastructure.commercial;

import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityQuery;
import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityResult;
import com.magyen.platform.commercial.application.dto.OrderResult;
import com.magyen.platform.commercial.application.port.OrderPaymentCollectionPort;
import com.magyen.platform.commercial.application.usecase.GetOrderProfitabilityUseCase;
import com.magyen.platform.commercial.application.usecase.GetOrdersUseCase;
import com.magyen.platform.commercial.domain.OrderProfitabilityStatus;
import com.magyen.platform.commercial.domain.OrderStatus;
import com.magyen.platform.home.application.port.CommercialDashboardPort;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
 * Rentabilidad: {@link GetOrderProfitabilityUseCase} (SPR-036) — no se recalculan fórmulas.
 * <p>
 * Órdenes elegibles para rentabilidad: CONFIRMED, IN_PRODUCTION, READY_FOR_DELIVERY, DELIVERED
 * (excluye CLOSED). No existe DRAFT en el dominio de Order.
 */
public class CommercialDashboardAdapter implements CommercialDashboardPort {

    private static final Set<OrderStatus> PROFITABILITY_ELIGIBLE_STATUSES = EnumSet.of(
            OrderStatus.CONFIRMED,
            OrderStatus.IN_PRODUCTION,
            OrderStatus.READY_FOR_DELIVERY,
            OrderStatus.DELIVERED
    );

    private static final Comparator<ReceivableItem> RECEIVABLE_ORDER = Comparator
            .comparing(ReceivableItem::outstandingAmount, Comparator.reverseOrder())
            .thenComparing(ReceivableItem::orderNumber, Comparator.nullsLast(String::compareTo))
            .thenComparing(item -> item.orderId().toString());

    private final GetOrdersUseCase getOrdersUseCase;
    private final OrderPaymentCollectionPort orderPaymentCollectionPort;
    private final GetOrderProfitabilityUseCase getOrderProfitabilityUseCase;

    public CommercialDashboardAdapter(
            GetOrdersUseCase getOrdersUseCase,
            OrderPaymentCollectionPort orderPaymentCollectionPort,
            GetOrderProfitabilityUseCase getOrderProfitabilityUseCase
    ) {
        this.getOrdersUseCase = Objects.requireNonNull(getOrdersUseCase, "Get orders use case must not be null");
        this.orderPaymentCollectionPort = Objects.requireNonNull(
                orderPaymentCollectionPort,
                "Order payment collection port must not be null"
        );
        this.getOrderProfitabilityUseCase = Objects.requireNonNull(
                getOrderProfitabilityUseCase,
                "Get order profitability use case must not be null"
        );
    }

    @Override
    public HomeReceivablesSnapshot getCurrentOutstandingReceivables() {
        List<ReceivableItem> outstandingItems = getOrdersUseCase.execute().orders().stream()
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
    public HomeProfitabilitySummarySnapshot getCurrentProfitabilitySummary() {
        List<OrderResult> eligibleOrders = getOrdersUseCase.execute().orders().stream()
                .filter(order -> order.status() != null
                        && PROFITABILITY_ELIGIBLE_STATUSES.contains(order.status()))
                .toList();

        int completeCount = 0;
        int partiallyUnvaluedCount = 0;
        int noCostDataCount = 0;
        int unvaluedCostCount = 0;
        BigDecimal totalOrderValue = BigDecimal.ZERO;
        BigDecimal totalDirectCost = BigDecimal.ZERO;
        BigDecimal totalDirectProfit = BigDecimal.ZERO;

        for (OrderResult order : eligibleOrders) {
            GetOrderProfitabilityResult profitability = getOrderProfitabilityUseCase.execute(
                    new GetOrderProfitabilityQuery(order.orderId())
            );
            unvaluedCostCount += Math.max(0, profitability.unvaluedMaterialConsumptionCount());

            OrderProfitabilityStatus status = profitability.profitabilityStatus();
            if (status == OrderProfitabilityStatus.COMPLETE) {
                completeCount++;
                totalOrderValue = totalOrderValue.add(nullToZero(profitability.orderValue()));
                totalDirectCost = totalDirectCost.add(nullToZero(profitability.totalDirectCost()));
                totalDirectProfit = totalDirectProfit.add(nullToZero(profitability.directProfit()));
            } else if (status == OrderProfitabilityStatus.PARTIALLY_UNVALUED) {
                partiallyUnvaluedCount++;
            } else if (status == OrderProfitabilityStatus.NO_COST_DATA) {
                noCostDataCount++;
            }
        }

        BigDecimal averageMargin = null;
        if (totalOrderValue.compareTo(BigDecimal.ZERO) > 0) {
            averageMargin = totalDirectProfit
                    .divide(totalOrderValue, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return new HomeProfitabilitySummarySnapshot(
                eligibleOrders.size(),
                completeCount,
                partiallyUnvaluedCount,
                noCostDataCount,
                totalOrderValue,
                totalDirectCost,
                totalDirectProfit,
                averageMargin,
                unvaluedCostCount
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
                order.customerId(),
                order.totalAmount(),
                collection.collectedAmount(),
                outstandingAmount
        ));
    }

    private static BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
