package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.CustomerNameResolver;
import com.magyen.platform.commercial.application.SellerNameResolver;
import com.magyen.platform.commercial.application.dto.GetOrdersQuery;
import com.magyen.platform.commercial.application.dto.GetOrdersResult;
import com.magyen.platform.commercial.application.dto.OrderResult;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderNumber;
import com.magyen.platform.commercial.domain.OrderProfitabilityEligibility;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.Quotation;
import com.magyen.platform.commercial.domain.QuotationNumber;
import com.magyen.platform.commercial.domain.QuotationNumberFormat;
import com.magyen.platform.commercial.domain.QuotationRepository;
import com.magyen.platform.commercial.domain.exception.OrderDomainException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Caso de uso que consulta las Órdenes existentes.
 */
public class GetOrdersUseCase {

    private static final Comparator<Order> BY_COMMERCIAL_CONSECUTIVE =
            Comparator.comparing(Order::getOrderNumber, OrderNumber.byCommercialConsecutive());

    private final OrderRepository orderRepository;
    private final QuotationRepository quotationRepository;
    private final SellerNameResolver sellerNameResolver;
    private final CustomerNameResolver customerNameResolver;

    public GetOrdersUseCase(
            OrderRepository orderRepository,
            QuotationRepository quotationRepository,
            SellerNameResolver sellerNameResolver,
            CustomerNameResolver customerNameResolver
    ) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "Order repository must not be null");
        this.quotationRepository = Objects.requireNonNull(
                quotationRepository,
                "Quotation repository must not be null"
        );
        this.sellerNameResolver = Objects.requireNonNull(sellerNameResolver, "Seller name resolver must not be null");
        this.customerNameResolver = Objects.requireNonNull(
                customerNameResolver,
                "Customer name resolver must not be null"
        );
    }

    public GetOrdersResult execute() {
        return execute(new GetOrdersQuery(null, null));
    }

    public GetOrdersResult execute(GetOrdersQuery query) {
        Objects.requireNonNull(query, "Query must not be null");
        validateRange(query.fromDate(), query.toDate());

        List<Order> orders = orderRepository.findAll().stream()
                .filter(order -> inRange(order.getConfirmationDate(), query.fromDate(), query.toDate()))
                .filter(order -> acceptsDirectCost(order, query.acceptsDirectCost()))
                .sorted(BY_COMMERCIAL_CONSECUTIVE)
                .toList();
        Function<UUID, String> sellerNames = sellerNameResolver.nameLookup(
                orders.stream().map(Order::getSellerId).toList()
        );
        Function<UUID, String> customerNames = customerNameResolver.nameLookup(
                orders.stream().map(Order::getCustomerId).toList()
        );
        orders = filterBySearch(orders, customerNames, query.search());
        orders = applyLimit(orders, query.limit());
        Map<UUID, QuotationNumber> quotationNumbers = quotationRepository.findAll().stream()
                .filter(quotation -> quotation.getQuotationNumber() != null)
                .collect(Collectors.toMap(Quotation::getId, Quotation::getQuotationNumber, (left, right) -> left));

        List<OrderResult> results = orders.stream()
                .map(order -> toOrderResult(
                        order,
                        sellerNames.apply(order.getSellerId()),
                        customerNames.apply(order.getCustomerId()),
                        quotationNumbers.get(order.getQuotationId())
                ))
                .toList();

        return new GetOrdersResult(results);
    }

    private static void validateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null && toDate == null) {
            return;
        }
        if (fromDate == null || toDate == null) {
            throw new OrderDomainException("Both fromDate and toDate must be provided together");
        }
        if (fromDate.isAfter(toDate)) {
            throw new OrderDomainException("From date must not be after to date");
        }
    }

    private static boolean acceptsDirectCost(Order order, Boolean acceptsDirectCost) {
        if (!Boolean.TRUE.equals(acceptsDirectCost)) {
            return true;
        }
        return OrderProfitabilityEligibility.includes(order.getStatus());
    }

    private static List<Order> filterBySearch(
            List<Order> orders,
            Function<UUID, String> customerNames,
            String search
    ) {
        if (search == null || search.isBlank()) {
            return orders;
        }
        String needle = search.trim().toLowerCase(Locale.ROOT);
        return orders.stream()
                .filter(order -> matchesSearch(order, customerNames.apply(order.getCustomerId()), needle))
                .toList();
    }

    private static boolean matchesSearch(Order order, String customerName, String needle) {
        String orderNumber = order.getOrderNumber() == null ? "" : order.getOrderNumber().getValue();
        String description = order.getDescription() == null ? "" : order.getDescription();
        String name = customerName == null ? "" : customerName;
        return orderNumber.toLowerCase(Locale.ROOT).contains(needle)
                || description.toLowerCase(Locale.ROOT).contains(needle)
                || name.toLowerCase(Locale.ROOT).contains(needle);
    }

    /**
     * Sin límite conserva el orden comercial ascendente del listado.
     * Con límite devuelve los últimos N, del más reciente al más antiguo.
     */
    private static List<Order> applyLimit(List<Order> orders, Integer limit) {
        if (limit == null) {
            return orders;
        }
        if (limit <= 0) {
            throw new OrderDomainException("Order search limit must be greater than zero");
        }
        if (orders.size() <= limit) {
            List<Order> newestFirst = new ArrayList<>(orders);
            newestFirst.sort(BY_COMMERCIAL_CONSECUTIVE.reversed());
            return List.copyOf(newestFirst);
        }
        List<Order> newest = new ArrayList<>(orders.subList(orders.size() - limit, orders.size()));
        newest.sort(BY_COMMERCIAL_CONSECUTIVE.reversed());
        return List.copyOf(newest);
    }

    private static boolean inRange(LocalDate businessDate, LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            return true;
        }
        return businessDate != null && !businessDate.isBefore(fromDate) && !businessDate.isAfter(toDate);
    }

    private OrderResult toOrderResult(
            Order order,
            String sellerName,
            String customerName,
            QuotationNumber quotationNumber
    ) {
        Long quotationNumberValue = quotationNumber == null ? null : quotationNumber.getValue();
        return new OrderResult(
                order.getId(),
                order.getOrderNumber().getValue(),
                order.getDescription(),
                order.getCustomerId(),
                customerName,
                order.getQuotationId(),
                quotationNumberValue,
                QuotationNumberFormat.display(quotationNumberValue),
                order.getConfirmationDate(),
                order.getDeliveryCommitment().getPromisedDeliveryDate(),
                order.getStatus(),
                order.getSellerId(),
                sellerName,
                order.getObservations(),
                order.getTotal().getAmount()
        );
    }
}
