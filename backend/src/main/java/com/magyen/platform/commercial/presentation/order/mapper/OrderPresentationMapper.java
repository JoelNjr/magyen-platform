package com.magyen.platform.commercial.presentation.order.mapper;

import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationResult;
import com.magyen.platform.commercial.application.dto.GetOrderCommand;
import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityListResult;
import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityQuery;
import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityResult;
import com.magyen.platform.commercial.application.dto.GetOrderResult;
import com.magyen.platform.commercial.application.dto.GetOrdersResult;
import com.magyen.platform.commercial.application.dto.OrderItemResult;
import com.magyen.platform.commercial.application.dto.OrderResult;
import com.magyen.platform.commercial.application.dto.ProductSpecificationCommand;
import com.magyen.platform.commercial.application.dto.ProductSpecificationResult;
import com.magyen.platform.commercial.application.dto.ReplaceOrderItemSizesCommand;
import com.magyen.platform.commercial.application.dto.ReplaceOrderItemSizesResult;
import com.magyen.platform.commercial.application.dto.SizeBreakdownCommand;
import com.magyen.platform.commercial.application.dto.SizeBreakdownResult;
import com.magyen.platform.commercial.application.dto.UpdateOrderItemProductSpecificationCommand;
import com.magyen.platform.commercial.application.dto.UpdateOrderItemProductSpecificationResult;
import com.magyen.platform.commercial.presentation.order.request.CreateOrderRequest;
import com.magyen.platform.commercial.presentation.order.request.ReplaceOrderItemSizesRequest;
import com.magyen.platform.commercial.presentation.order.request.SizeBreakdownRequest;
import com.magyen.platform.commercial.presentation.order.request.UpdateOrderItemProductSpecificationRequest;
import com.magyen.platform.commercial.presentation.order.response.CreateOrderResponse;
import com.magyen.platform.commercial.presentation.order.response.GetOrderProfitabilityListResponse;
import com.magyen.platform.commercial.presentation.order.response.GetOrderProfitabilityResponse;
import com.magyen.platform.commercial.presentation.order.response.GetOrderResponse;
import com.magyen.platform.commercial.presentation.order.response.GetOrderResponse.DeliveryCommitmentResponse;
import com.magyen.platform.commercial.presentation.order.response.GetOrderResponse.PaymentSummaryResponse;
import com.magyen.platform.commercial.presentation.order.response.GetOrdersResponse;
import com.magyen.platform.commercial.presentation.order.response.GetOrdersResponse.OrderResponse;
import com.magyen.platform.commercial.presentation.order.response.OrderItemResponse;
import com.magyen.platform.commercial.presentation.order.response.ReplaceOrderItemSizesResponse;
import com.magyen.platform.commercial.presentation.order.response.SizeBreakdownResponse;
import com.magyen.platform.commercial.presentation.order.response.UpdateOrderItemProductSpecificationResponse;
import com.magyen.platform.commercial.presentation.quotation.response.ProductSpecificationResponse;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Convierte entre objetos HTTP de Presentation y DTOs de Application.
 * <p>
 * No contiene reglas de negocio ni accede a repositorios, dominio o infraestructura.
 */
public class OrderPresentationMapper {

    public CreateOrderFromQuotationCommand toCommand(CreateOrderRequest request) {
        Objects.requireNonNull(request, "CreateOrderRequest must not be null");

        return new CreateOrderFromQuotationCommand(
                request.quotationId(),
                request.description(),
                request.confirmationDate(),
                request.deliveryDate(),
                request.observations()
        );
    }

    public CreateOrderResponse toResponse(CreateOrderFromQuotationResult result) {
        Objects.requireNonNull(result, "CreateOrderFromQuotationResult must not be null");

        return new CreateOrderResponse(
                result.orderId(),
                result.orderNumber(),
                result.status().name(),
                result.confirmationDate()
        );
    }

    public GetOrdersResponse toResponse(GetOrdersResult result) {
        Objects.requireNonNull(result, "GetOrdersResult must not be null");

        List<OrderResponse> orders = result.orders().stream()
                .map(this::toOrderResponse)
                .toList();

        return new GetOrdersResponse(orders);
    }

    public GetOrderCommand toGetOrderCommand(UUID orderId) {
        Objects.requireNonNull(orderId, "Order id must not be null");

        return new GetOrderCommand(orderId);
    }

    public GetOrderProfitabilityQuery toGetOrderProfitabilityQuery(UUID orderId) {
        Objects.requireNonNull(orderId, "Order id must not be null");

        return new GetOrderProfitabilityQuery(orderId);
    }

    public GetOrderProfitabilityResponse toResponse(GetOrderProfitabilityResult result) {
        Objects.requireNonNull(result, "GetOrderProfitabilityResult must not be null");

        return new GetOrderProfitabilityResponse(
                result.orderId(),
                result.orderNumber(),
                result.description(),
                result.customerName(),
                result.promisedDeliveryDate(),
                result.orderValue(),
                result.collectedAmount(),
                result.outstandingAmount(),
                result.materialCost(),
                result.laborCost(),
                result.plotterMaterialCost(),
                result.plotterCostAttributable(),
                result.internalPlotterServiceCost(),
                result.totalDirectCost(),
                result.directProfit(),
                result.directMarginPercentage(),
                result.unvaluedMaterialConsumptionCount(),
                result.profitabilityStatus().name()
        );
    }

    public GetOrderProfitabilityListResponse toResponse(GetOrderProfitabilityListResult result) {
        Objects.requireNonNull(result, "GetOrderProfitabilityListResult must not be null");
        var summary = result.summary();
        return new GetOrderProfitabilityListResponse(
                result.orders().stream().map(this::toResponse).toList(),
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

    public GetOrderResponse toResponse(GetOrderResult result) {
        Objects.requireNonNull(result, "GetOrderResult must not be null");

        List<OrderItemResponse> items = result.items().stream()
                .map(this::toItemResponse)
                .toList();

        return new GetOrderResponse(
                result.orderId(),
                result.orderNumber(),
                result.description(),
                result.customerId(),
                result.customerName(),
                result.quotationId(),
                result.quotationNumber(),
                result.quotationNumberDisplay(),
                result.confirmationDate(),
                result.status().name(),
                new DeliveryCommitmentResponse(
                        result.deliveryCommitment().promisedDeliveryDate(),
                        result.deliveryCommitment().deliveryObservations()
                ),
                new PaymentSummaryResponse(
                        result.paymentSummary().advanceAcknowledged(),
                        result.paymentSummary().finalPaymentAcknowledged(),
                        result.paymentSummary().committedTotal(),
                        result.paymentSummary().remainingBalance()
                ),
                result.sellerId(),
                result.sellerName(),
                result.observations(),
                items,
                result.totalAmount()
        );
    }

    private OrderResponse toOrderResponse(OrderResult order) {
        return new OrderResponse(
                order.orderId(),
                order.orderNumber(),
                order.description(),
                order.customerId(),
                order.customerName(),
                order.quotationId(),
                order.quotationNumber(),
                order.quotationNumberDisplay(),
                order.confirmationDate(),
                order.status().name(),
                order.sellerId(),
                order.sellerName(),
                order.observations(),
                order.totalAmount()
        );
    }

    public ReplaceOrderItemSizesCommand toReplaceOrderItemSizesCommand(
            UUID orderId,
            UUID orderItemId,
            ReplaceOrderItemSizesRequest request
    ) {
        Objects.requireNonNull(orderId, "Order id must not be null");
        Objects.requireNonNull(orderItemId, "Order item id must not be null");
        Objects.requireNonNull(request, "ReplaceOrderItemSizesRequest must not be null");

        List<SizeBreakdownRequest> sizes = request.sizes() == null
                ? Collections.emptyList()
                : request.sizes();

        return new ReplaceOrderItemSizesCommand(
                orderId,
                orderItemId,
                sizes.stream()
                        .map(this::toSizeBreakdownCommand)
                        .toList()
        );
    }

    public ReplaceOrderItemSizesResponse toResponse(ReplaceOrderItemSizesResult result) {
        Objects.requireNonNull(result, "ReplaceOrderItemSizesResult must not be null");

        return new ReplaceOrderItemSizesResponse(
                result.orderItemId(),
                result.sizes().stream()
                        .map(this::toSizeBreakdownResponse)
                        .toList()
        );
    }

    public UpdateOrderItemProductSpecificationCommand toUpdateOrderItemProductSpecificationCommand(
            UUID orderId,
            UUID orderItemId,
            UpdateOrderItemProductSpecificationRequest request
    ) {
        Objects.requireNonNull(orderId, "Order id must not be null");
        Objects.requireNonNull(orderItemId, "Order item id must not be null");
        Objects.requireNonNull(request, "UpdateOrderItemProductSpecificationRequest must not be null");

        return new UpdateOrderItemProductSpecificationCommand(
                orderId,
                orderItemId,
                new ProductSpecificationCommand(
                        request.garmentType(),
                        request.collarType(),
                        request.sleeveType(),
                        request.cuffRequired(),
                        booleanOrFalse(request.sublimationRequired()),
                        booleanOrFalse(request.embroideryRequired()),
                        booleanOrFalse(request.dtfRequired()),
                        request.decorationNotes(),
                        booleanOrFalse(request.includesNames()),
                        booleanOrFalse(request.includesNumbers()),
                        booleanOrFalse(request.includesLogos()),
                        request.personalizationNotes(),
                        request.itemObservations()
                )
        );
    }

    public UpdateOrderItemProductSpecificationResponse toResponse(
            UpdateOrderItemProductSpecificationResult result
    ) {
        Objects.requireNonNull(result, "UpdateOrderItemProductSpecificationResult must not be null");

        return new UpdateOrderItemProductSpecificationResponse(
                result.orderItemId(),
                toProductSpecificationResponse(result.productSpecification())
        );
    }

    private OrderItemResponse toItemResponse(OrderItemResult item) {
        List<SizeBreakdownResponse> sizes = item.sizes() == null
                ? List.of()
                : item.sizes().stream()
                        .map(this::toSizeBreakdownResponse)
                        .toList();

        return new OrderItemResponse(
                item.itemId(),
                item.productName(),
                item.quantity(),
                item.fabric(),
                item.secondaryFabric(),
                item.color(),
                item.unitPrice(),
                item.subtotal(),
                toProductSpecificationResponse(item.productSpecification()),
                sizes
        );
    }

    private SizeBreakdownCommand toSizeBreakdownCommand(SizeBreakdownRequest request) {
        Objects.requireNonNull(request, "SizeBreakdownRequest must not be null");
        return new SizeBreakdownCommand(request.size(), request.quantity());
    }

    private SizeBreakdownResponse toSizeBreakdownResponse(SizeBreakdownResult result) {
        return new SizeBreakdownResponse(result.size(), result.quantity());
    }

    private ProductSpecificationResponse toProductSpecificationResponse(ProductSpecificationResult result) {
        ProductSpecificationResult resolved = result == null
                ? new ProductSpecificationResult(
                        null, null, null, null,
                        false, false, false, null,
                        false, false, false, null, null
                )
                : result;

        return new ProductSpecificationResponse(
                resolved.garmentType(),
                resolved.collarType(),
                resolved.sleeveType(),
                resolved.cuffRequired(),
                resolved.sublimationRequired(),
                resolved.embroideryRequired(),
                resolved.dtfRequired(),
                resolved.decorationNotes(),
                resolved.includesNames(),
                resolved.includesNumbers(),
                resolved.includesLogos(),
                resolved.personalizationNotes(),
                resolved.itemObservations()
        );
    }

    private static boolean booleanOrFalse(Boolean value) {
        return value != null && value;
    }
}
