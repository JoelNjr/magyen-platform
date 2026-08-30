package com.magyen.platform.commercial.application.usecase;

import com.magyen.platform.commercial.application.CustomerNameResolver;
import com.magyen.platform.commercial.application.SellerNameResolver;
import com.magyen.platform.commercial.application.dto.DeliveryCommitmentResult;
import com.magyen.platform.commercial.application.dto.GetOrderCommand;
import com.magyen.platform.commercial.application.dto.GetOrderResult;
import com.magyen.platform.commercial.application.dto.OrderItemResult;
import com.magyen.platform.commercial.application.dto.PaymentSummaryResult;
import com.magyen.platform.commercial.application.dto.ProductSpecificationResult;
import com.magyen.platform.commercial.application.dto.SizeBreakdownResult;
import com.magyen.platform.commercial.domain.DeliveryCommitment;
import com.magyen.platform.commercial.domain.Order;
import com.magyen.platform.commercial.domain.OrderItem;
import com.magyen.platform.commercial.domain.OrderRepository;
import com.magyen.platform.commercial.domain.PaymentSummary;
import com.magyen.platform.commercial.domain.ProductSpecification;
import com.magyen.platform.commercial.domain.Quotation;
import com.magyen.platform.commercial.domain.QuotationNumber;
import com.magyen.platform.commercial.domain.QuotationNumberFormat;
import com.magyen.platform.commercial.domain.QuotationRepository;
import com.magyen.platform.commercial.domain.SizeBreakdown;

import java.util.List;
import java.util.Objects;

/**
 * Caso de uso que consulta una Orden completa por identificador.
 */
public class GetOrderUseCase {

    private final OrderRepository orderRepository;
    private final QuotationRepository quotationRepository;
    private final SellerNameResolver sellerNameResolver;
    private final CustomerNameResolver customerNameResolver;

    public GetOrderUseCase(
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

    public GetOrderResult execute(GetOrderCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        validateCommand(command);

        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order not found: " + command.orderId()
                ));

        return toResult(order);
    }

    private void validateCommand(GetOrderCommand command) {
        Objects.requireNonNull(command.orderId(), "Order id must not be null");
    }

    private GetOrderResult toResult(Order order) {
        List<OrderItemResult> items = order.getItems().stream()
                .map(this::toItemResult)
                .toList();

        Long quotationNumberValue = quotationRepository.findById(order.getQuotationId())
                .map(Quotation::getQuotationNumber)
                .map(QuotationNumber::getValue)
                .orElse(null);

        return new GetOrderResult(
                order.getId(),
                order.getOrderNumber().getValue(),
                order.getDescription(),
                order.getCustomerId(),
                customerNameResolver.resolveName(order.getCustomerId()),
                order.getQuotationId(),
                quotationNumberValue,
                QuotationNumberFormat.display(quotationNumberValue),
                order.getConfirmationDate(),
                order.getStatus(),
                toDeliveryCommitmentResult(order.getDeliveryCommitment()),
                toPaymentSummaryResult(order.getPaymentSummary()),
                order.getSellerId(),
                sellerNameResolver.resolveName(order.getSellerId()),
                order.getObservations(),
                items,
                order.getSubtotal().getAmount(),
                order.getDiscount().getAmount(),
                order.getTotal().getAmount()
        );
    }

    private OrderItemResult toItemResult(OrderItem item) {
        return new OrderItemResult(
                item.getId(),
                item.getProductName(),
                item.getQuantity(),
                item.getFabric(),
                item.getSecondaryFabric(),
                item.getColor(),
                item.getUnitPrice().getAmount(),
                item.getSubtotal().getAmount(),
                toProductSpecificationResult(item.getProductSpecification()),
                item.getSizeBreakdowns().stream()
                        .map(this::toSizeBreakdownResult)
                        .toList()
        );
    }

    private SizeBreakdownResult toSizeBreakdownResult(SizeBreakdown sizeBreakdown) {
        return new SizeBreakdownResult(
                sizeBreakdown.getSize(),
                sizeBreakdown.getQuantity()
        );
    }

    private ProductSpecificationResult toProductSpecificationResult(ProductSpecification specification) {
        ProductSpecification resolved = specification == null ? ProductSpecification.empty() : specification;

        return new ProductSpecificationResult(
                resolved.getGarmentType(),
                resolved.getCollarType(),
                resolved.getSleeveType(),
                resolved.getCuffRequired(),
                resolved.isSublimationRequired(),
                resolved.isEmbroideryRequired(),
                resolved.isDtfRequired(),
                resolved.getDecorationNotes(),
                resolved.isIncludesNames(),
                resolved.isIncludesNumbers(),
                resolved.isIncludesLogos(),
                resolved.getPersonalizationNotes(),
                resolved.getItemObservations()
        );
    }

    private DeliveryCommitmentResult toDeliveryCommitmentResult(DeliveryCommitment deliveryCommitment) {
        return new DeliveryCommitmentResult(
                deliveryCommitment.getPromisedDeliveryDate(),
                deliveryCommitment.getDeliveryObservations()
        );
    }

    private PaymentSummaryResult toPaymentSummaryResult(PaymentSummary paymentSummary) {
        return new PaymentSummaryResult(
                paymentSummary.isAdvanceAcknowledged(),
                paymentSummary.isFinalPaymentAcknowledged(),
                paymentSummary.getCommittedTotal().getAmount(),
                paymentSummary.getRemainingBalance().getAmount()
        );
    }
}
