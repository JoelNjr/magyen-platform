package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.dto.GetSellerCommissionQuery;
import com.magyen.platform.commercial.application.dto.GetSellerCommissionResult;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.SellerCommissionPolicy;
import com.magyen.platform.commercial.domain.exception.OrderDomainException;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Calcula la comisión V1 (5 %) analítica de los pedidos del vendedor.
 * <p>
 * No crea INCOME ni EXPENSE. No altera rentabilidad del pedido.
 */
public class GetSellerCommissionPerformanceUseCase {

    private final OrderRepository orderRepository;

    public GetSellerCommissionPerformanceUseCase(OrderRepository orderRepository) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "Order repository must not be null");
    }

    public GetSellerCommissionResult execute(GetSellerCommissionQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        Objects.requireNonNull(query.sellerEmployeeId(), "Seller employee id must not be null");
        validateRange(query);

        UUID sellerEmployeeId = query.sellerEmployeeId();
        BigDecimal totalSales = SellerCommissionPolicy.money(BigDecimal.ZERO);
        int eligibleCount = 0;

        for (Order order : orderRepository.findAll()) {
            if (!sellerEmployeeId.equals(order.getSellerId())) {
                continue;
            }
            if (!SellerCommissionPolicy.includes(order.getStatus())) {
                continue;
            }
            if (!SellerCommissionPolicy.confirmationDateInRange(
                    order.getConfirmationDate(),
                    query.fromDate(),
                    query.toDate()
            )) {
                continue;
            }
            eligibleCount++;
            totalSales = totalSales.add(SellerCommissionPolicy.money(order.getTotal().getAmount()));
        }

        return new GetSellerCommissionResult(
                sellerEmployeeId,
                query.fromDate(),
                query.toDate(),
                eligibleCount,
                SellerCommissionPolicy.money(totalSales),
                SellerCommissionPolicy.RATE_PERCENTAGE,
                SellerCommissionPolicy.commissionOnSales(totalSales)
        );
    }

    private static void validateRange(GetSellerCommissionQuery query) {
        if (query.fromDate() == null && query.toDate() == null) {
            return;
        }
        if (query.fromDate() == null || query.toDate() == null) {
            throw new OrderDomainException("Both fromDate and toDate must be provided together");
        }
        if (query.fromDate().isAfter(query.toDate())) {
            throw new OrderDomainException("From date must not be after to date");
        }
    }
}
