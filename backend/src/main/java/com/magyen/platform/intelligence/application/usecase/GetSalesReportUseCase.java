package com.magyen.platform.intelligence.application.usecase;

import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.intelligence.application.dto.GetSalesReportResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * Caso de uso que consolida el reporte de ventas a partir de las Órdenes comerciales.
 * <p>
 * Solo consulta información existente; no modifica el estado del negocio.
 */
public class GetSalesReportUseCase {

    private static final int MONEY_SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private final OrderRepository orderRepository;

    public GetSalesReportUseCase(OrderRepository orderRepository) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "Order repository must not be null");
    }

    public GetSalesReportResult execute() {
        List<Order> orders = orderRepository.findAll();

        BigDecimal totalSold = orders.stream()
                .map(order -> order.getTotal().getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long orderCount = orders.size();
        BigDecimal averagePerSale = calculateAverage(totalSold, orderCount);

        return new GetSalesReportResult(totalSold, orderCount, averagePerSale);
    }

    private BigDecimal calculateAverage(BigDecimal total, long count) {
        if (count == 0) {
            return BigDecimal.ZERO.setScale(MONEY_SCALE, ROUNDING_MODE);
        }
        return total.divide(BigDecimal.valueOf(count), MONEY_SCALE, ROUNDING_MODE);
    }
}
