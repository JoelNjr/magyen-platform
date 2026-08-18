package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.CustomerNameResolver;
import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityQuery;
import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityResult;
import com.magyen.platform.commercial.application.port.OrderPaymentCollectionPort;
import com.magyen.platform.commercial.application.port.PlotterOrderCostPort;
import com.magyen.platform.commercial.application.port.ProductionOrderCostPort;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderProfitabilityStatus;
import com.magyen.platform.commercial.domain.OrderRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Calcula la rentabilidad directa de una Orden comercial (fundamento de costos).
 * <p>
 * Solo lectura: no registra FinancialTransaction ni altera Inventory/Production.
 * <ul>
 *   <li>Valor de orden = {@code Order.getTotal()} (no caja cobrada)</li>
 *   <li>Cobrado = suma de Payments Finance por orderId</li>
 *   <li>Material = costo histórico Inventory vía atribución Production</li>
 *   <li>Mano de obra = suma PENDING+PAID (CANCELLED excluido)</li>
 *   <li>Plotter interno = snapshot histórico del OUT de papel (no el EXPENSE de compra)</li>
 * </ul>
 */
public class GetOrderProfitabilityUseCase {

    private static final BigDecimal ZERO_MONEY = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final OrderRepository orderRepository;
    private final CustomerNameResolver customerNameResolver;
    private final OrderPaymentCollectionPort orderPaymentCollectionPort;
    private final ProductionOrderCostPort productionOrderCostPort;
    private final PlotterOrderCostPort plotterOrderCostPort;

    public GetOrderProfitabilityUseCase(
            OrderRepository orderRepository,
            CustomerNameResolver customerNameResolver,
            OrderPaymentCollectionPort orderPaymentCollectionPort,
            ProductionOrderCostPort productionOrderCostPort,
            PlotterOrderCostPort plotterOrderCostPort
    ) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "Order repository must not be null");
        this.customerNameResolver = Objects.requireNonNull(
                customerNameResolver,
                "Customer name resolver must not be null"
        );
        this.orderPaymentCollectionPort = Objects.requireNonNull(
                orderPaymentCollectionPort,
                "Order payment collection port must not be null"
        );
        this.productionOrderCostPort = Objects.requireNonNull(
                productionOrderCostPort,
                "Production order cost port must not be null"
        );
        this.plotterOrderCostPort = Objects.requireNonNull(
                plotterOrderCostPort,
                "Plotter order cost port must not be null"
        );
    }

    public GetOrderProfitabilityResult execute(GetOrderProfitabilityQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        Objects.requireNonNull(query.orderId(), "Order id must not be null");

        Order order = orderRepository.findById(query.orderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order not found: " + query.orderId()
                ));

        BigDecimal orderValue = money(order.getTotal().getAmount());

        OrderPaymentCollectionPort.OrderPaymentCollection collection =
                orderPaymentCollectionPort.getCollection(order.getId());

        ProductionOrderCostPort.ProductionOrderCostSnapshot costs =
                productionOrderCostPort.findCostsByOrderId(order.getId());
        PlotterOrderCostPort.PlotterOrderCostSnapshot plotterCosts =
                plotterOrderCostPort.findCostsByOrderId(order.getId());

        BigDecimal materialCost = money(costs.materialCost());
        BigDecimal laborCost = money(costs.laborCost());
        BigDecimal plotterMaterialCost = money(plotterCosts.plotterMaterialCost());
        BigDecimal collectedAmount = money(collection.collectedAmount());
        BigDecimal outstandingAmount = money(collection.outstandingAmount());
        BigDecimal totalDirectCost = money(materialCost.add(laborCost).add(plotterMaterialCost));
        BigDecimal directProfit = money(orderValue.subtract(totalDirectCost));
        BigDecimal directMarginPercentage = resolveMarginPercentage(orderValue, directProfit);

        int unvaluedCount = costs.unvaluedMaterialConsumptionCount() + plotterCosts.unvaluedJobCount();
        OrderProfitabilityStatus status = resolveStatus(costs, plotterCosts);

        return new GetOrderProfitabilityResult(
                order.getId(),
                orderValue,
                collectedAmount,
                outstandingAmount,
                materialCost,
                laborCost,
                plotterMaterialCost,
                plotterCosts.plotterCostAttributable(),
                totalDirectCost,
                directProfit,
                directMarginPercentage,
                unvaluedCount,
                status,
                order.getOrderNumber().getValue(),
                order.getDescription(),
                customerNameResolver.resolveName(order.getCustomerId()),
                order.getDeliveryCommitment().getPromisedDeliveryDate()
        );
    }

    private static BigDecimal money(BigDecimal amount) {
        if (amount == null) {
            return ZERO_MONEY;
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal resolveMarginPercentage(BigDecimal orderValue, BigDecimal directProfit) {
        if (orderValue.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return directProfit
                .divide(orderValue, 4, RoundingMode.HALF_UP)
                .multiply(HUNDRED)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private static OrderProfitabilityStatus resolveStatus(
            ProductionOrderCostPort.ProductionOrderCostSnapshot costs,
            PlotterOrderCostPort.PlotterOrderCostSnapshot plotterCosts
    ) {
        boolean hasProductionActivity = costs.productionOrderFound()
                && (costs.materialConsumptionCount() > 0 || costs.laborWorkCount() > 0);
        boolean hasPlotterActivity = plotterCosts.internalJobCount() > 0;
        if (!hasProductionActivity && !hasPlotterActivity) {
            return OrderProfitabilityStatus.NO_COST_DATA;
        }
        if (costs.unvaluedMaterialConsumptionCount() > 0 || plotterCosts.unvaluedJobCount() > 0) {
            return OrderProfitabilityStatus.PARTIALLY_UNVALUED;
        }
        return OrderProfitabilityStatus.COMPLETE;
    }
}
