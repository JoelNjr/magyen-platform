package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.CustomerNameResolver;
import com.magyen.platform.commercial.application.dto.GetSellerCommissionQuery;
import com.magyen.platform.commercial.application.dto.GetSellerCommissionResult;
import com.magyen.platform.commercial.application.dto.SellerCommissionOrderLine;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.SellerCommissionPolicy;
import com.magyen.platform.commercial.domain.SellerCommissionReadStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Calcula la comisión mensual (5 %) de los pedidos confirmados en ese mes.
 * <p>
 * No crea INCOME ni EXPENSE. No liquida al vendedor.
 */
public class GetSellerCommissionPerformanceUseCase {

    private final OrderRepository orderRepository;
    private final CustomerNameResolver customerNameResolver;

    public GetSellerCommissionPerformanceUseCase(
            OrderRepository orderRepository,
            CustomerNameResolver customerNameResolver
    ) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "Order repository must not be null");
        this.customerNameResolver = Objects.requireNonNull(
                customerNameResolver,
                "Customer name resolver must not be null"
        );
    }

    public GetSellerCommissionResult execute(GetSellerCommissionQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        Objects.requireNonNull(query.sellerEmployeeId(), "Seller employee id must not be null");
        SellerCommissionPolicy.requireCalendarMonth(query.fromDate(), query.toDate());
        return toResult(query.sellerEmployeeId(), query.fromDate(), query.toDate(), matchingOrders(query));
    }

    public List<GetSellerCommissionResult> listForMonth(LocalDate fromDate, LocalDate toDate) {
        SellerCommissionPolicy.requireCalendarMonth(fromDate, toDate);
        List<UUID> sellerIds = orderRepository.findAll().stream()
                .filter(order -> SellerCommissionPolicy.confirmationDateInRange(
                        order.getConfirmationDate(),
                        fromDate,
                        toDate
                ))
                .map(Order::getSellerId)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.comparing(UUID::toString))
                .toList();

        List<GetSellerCommissionResult> results = new ArrayList<>();
        for (UUID sellerId : sellerIds) {
            GetSellerCommissionResult result = execute(new GetSellerCommissionQuery(sellerId, fromDate, toDate));
            if (result.numberOfEligibleOrders() > 0) {
                results.add(result);
            }
        }
        return List.copyOf(results);
    }

    /**
     * Acumulado del resumen de empleado cuando no hay un mes de comisión.
     * La pantalla de comisión no usa este camino.
     */
    public GetSellerCommissionResult executeUnbounded(GetSellerCommissionQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        Objects.requireNonNull(query.sellerEmployeeId(), "Seller employee id must not be null");
        if (query.fromDate() == null || query.toDate() == null) {
            return toResult(query.sellerEmployeeId(), null, null, matchingOrders(query));
        }
        return execute(query);
    }

    private List<Order> matchingOrders(GetSellerCommissionQuery query) {
        return orderRepository.findAll().stream()
                .filter(order -> query.sellerEmployeeId().equals(order.getSellerId()))
                .filter(order -> SellerCommissionPolicy.confirmationDateInRange(
                        order.getConfirmationDate(),
                        query.fromDate(),
                        query.toDate()
                ))
                .sorted(Comparator.comparing(Order::getConfirmationDate)
                        .thenComparing(order -> order.getOrderNumber().getValue()))
                .toList();
    }

    private GetSellerCommissionResult toResult(
            UUID sellerEmployeeId,
            LocalDate fromDate,
            LocalDate toDate,
            List<Order> orders
    ) {
        List<SellerCommissionOrderLine> lines = new ArrayList<>();
        BigDecimal totalSales = SellerCommissionPolicy.money(BigDecimal.ZERO);
        BigDecimal totalCommission = SellerCommissionPolicy.money(BigDecimal.ZERO);
        for (Order order : orders) {
            BigDecimal orderTotal = SellerCommissionPolicy.money(order.getTotal().getAmount());
            BigDecimal commissionAmount = SellerCommissionPolicy.commissionForOrder(orderTotal);
            totalSales = totalSales.add(orderTotal);
            totalCommission = totalCommission.add(commissionAmount);
            lines.add(new SellerCommissionOrderLine(
                    order.getId(),
                    order.getOrderNumber().getValue(),
                    customerNameResolver.resolveName(order.getCustomerId()),
                    order.getConfirmationDate(),
                    orderTotal,
                    SellerCommissionPolicy.RATE_PERCENTAGE,
                    commissionAmount
            ));
        }
        SellerCommissionReadStatus status = fromDate == null
                ? SellerCommissionReadStatus.HISTORICAL
                : SellerCommissionPolicy.readStatus(fromDate);
        return new GetSellerCommissionResult(
                sellerEmployeeId,
                fromDate,
                toDate,
                lines.size(),
                SellerCommissionPolicy.money(totalSales),
                SellerCommissionPolicy.RATE_PERCENTAGE,
                SellerCommissionPolicy.money(totalCommission),
                status,
                List.copyOf(lines)
        );
    }
}
