package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityQuery;
import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityResult;
import com.magyen.platform.commercial.application.port.OrderPaymentCollectionPort;
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
 *   <li>Plotter diferido: sin orderId confiable en PlotterJob → costo 0 y no atribuible</li>
 * </ul>
 */
public class GetOrderProfitabilityUseCase {

    private static final BigDecimal ZERO_MONEY = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final OrderRepository orderRepository;
    private final OrderPaymentCollectionPort orderPaymentCollectionPort;
    private final ProductionOrderCostPort productionOrderCostPort;

    public GetOrderProfitabilityUseCase(
            OrderRepository orderRepository,
            OrderPaymentCollectionPort orderPaymentCollectionPort,
            ProductionOrderCostPort productionOrderCostPort
    ) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "Order repository must not be null");
        this.orderPaymentCollectionPort = Objects.requireNonNull(
                orderPaymentCollectionPort,
                "Order payment collection port must not be null"
        );
        this.productionOrderCostPort = Objects.requireNonNull(
                productionOrderCostPort,
                "Production order cost port must not be null"
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

        // Diferido: PlotterJob aún no tiene orderId confiable para atribución.
        BigDecimal plotterMaterialCost = ZERO_MONEY;
        boolean plotterCostAttributable = false;

        BigDecimal materialCost = money(costs.materialCost());
        BigDecimal laborCost = money(costs.laborCost());
        BigDecimal collectedAmount = money(collection.collectedAmount());
        BigDecimal outstandingAmount = money(collection.outstandingAmount());
        BigDecimal totalDirectCost = money(materialCost.add(laborCost).add(plotterMaterialCost));
        BigDecimal directProfit = money(orderValue.subtract(totalDirectCost));
        BigDecimal directMarginPercentage = resolveMarginPercentage(orderValue, directProfit);

        OrderProfitabilityStatus status = resolveStatus(costs);

        return new GetOrderProfitabilityResult(
                order.getId(),
                orderValue,
                collectedAmount,
                outstandingAmount,
                materialCost,
                laborCost,
                plotterMaterialCost,
                plotterCostAttributable,
                totalDirectCost,
                directProfit,
                directMarginPercentage,
                costs.unvaluedMaterialConsumptionCount(),
                status
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
            ProductionOrderCostPort.ProductionOrderCostSnapshot costs
    ) {
        if (!costs.productionOrderFound()
                || (costs.materialConsumptionCount() == 0 && costs.laborWorkCount() == 0)) {
            return OrderProfitabilityStatus.NO_COST_DATA;
        }
        if (costs.unvaluedMaterialConsumptionCount() > 0) {
            return OrderProfitabilityStatus.PARTIALLY_UNVALUED;
        }
        return OrderProfitabilityStatus.COMPLETE;
    }
}
