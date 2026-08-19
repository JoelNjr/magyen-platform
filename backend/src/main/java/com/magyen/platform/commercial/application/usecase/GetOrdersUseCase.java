package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.CustomerNameResolver;
import com.magyen.platform.commercial.application.SellerNameResolver;
import com.magyen.platform.commercial.application.dto.GetOrdersQuery;
import com.magyen.platform.commercial.application.dto.GetOrdersResult;
import com.magyen.platform.commercial.application.dto.OrderResult;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.Quotation;
import com.magyen.platform.commercial.domain.QuotationNumber;
import com.magyen.platform.commercial.domain.QuotationNumberFormat;
import com.magyen.platform.commercial.domain.QuotationRepository;
import com.magyen.platform.commercial.domain.exception.OrderDomainException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Caso de uso que consulta las Órdenes existentes.
 */
public class GetOrdersUseCase {

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
                .toList();
        Function<UUID, String> sellerNames = sellerNameResolver.nameLookup(
                orders.stream().map(Order::getSellerId).toList()
        );
        Function<UUID, String> customerNames = customerNameResolver.nameLookup(
                orders.stream().map(Order::getCustomerId).toList()
        );
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
