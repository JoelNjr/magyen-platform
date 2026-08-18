package com.magyen.platform.commercial.presentation.order.controller;

import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationResult;
import com.magyen.platform.commercial.application.dto.GetOrderCommand;
import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityListResult;
import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityQuery;
import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityResult;
import com.magyen.platform.commercial.application.dto.GetOrderResult;
import com.magyen.platform.commercial.application.dto.GetOrdersResult;
import com.magyen.platform.commercial.application.dto.ReplaceOrderItemSizesCommand;
import com.magyen.platform.commercial.application.dto.ReplaceOrderItemSizesResult;
import com.magyen.platform.commercial.application.dto.UpdateOrderItemProductSpecificationCommand;
import com.magyen.platform.commercial.application.dto.UpdateOrderItemProductSpecificationResult;
import com.magyen.platform.commercial.application.usecase.CreateOrderFromQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.GetOrderProfitabilityListUseCase;
import com.magyen.platform.commercial.application.usecase.GetOrderProfitabilityUseCase;
import com.magyen.platform.commercial.application.usecase.GetOrderUseCase;
import com.magyen.platform.commercial.application.usecase.GetOrdersUseCase;
import com.magyen.platform.commercial.application.usecase.ReplaceOrderItemSizesUseCase;
import com.magyen.platform.commercial.application.usecase.UpdateOrderItemProductSpecificationUseCase;
import com.magyen.platform.commercial.presentation.order.mapper.OrderPresentationMapper;
import com.magyen.platform.commercial.presentation.order.request.CreateOrderRequest;
import com.magyen.platform.commercial.presentation.order.request.ReplaceOrderItemSizesRequest;
import com.magyen.platform.commercial.presentation.order.request.UpdateOrderItemProductSpecificationRequest;
import com.magyen.platform.commercial.presentation.order.response.CreateOrderResponse;
import com.magyen.platform.commercial.presentation.order.response.GetOrderProfitabilityListResponse;
import com.magyen.platform.commercial.presentation.order.response.GetOrderProfitabilityResponse;
import com.magyen.platform.commercial.presentation.order.response.GetOrderResponse;
import com.magyen.platform.commercial.presentation.order.response.GetOrdersResponse;
import com.magyen.platform.commercial.presentation.order.response.ReplaceOrderItemSizesResponse;
import com.magyen.platform.commercial.presentation.order.response.UpdateOrderItemProductSpecificationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Expone la API REST de órdenes.
 * <p>
 * Coordina HTTP con Application; no contiene reglas de negocio.
 */
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final CreateOrderFromQuotationUseCase createOrderFromQuotationUseCase;
    private final GetOrdersUseCase getOrdersUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final GetOrderProfitabilityUseCase getOrderProfitabilityUseCase;
    private final GetOrderProfitabilityListUseCase getOrderProfitabilityListUseCase;
    private final ReplaceOrderItemSizesUseCase replaceOrderItemSizesUseCase;
    private final UpdateOrderItemProductSpecificationUseCase updateOrderItemProductSpecificationUseCase;
    private final OrderPresentationMapper orderPresentationMapper;

    public OrderController(
            CreateOrderFromQuotationUseCase createOrderFromQuotationUseCase,
            GetOrdersUseCase getOrdersUseCase,
            GetOrderUseCase getOrderUseCase,
            GetOrderProfitabilityUseCase getOrderProfitabilityUseCase,
            GetOrderProfitabilityListUseCase getOrderProfitabilityListUseCase,
            ReplaceOrderItemSizesUseCase replaceOrderItemSizesUseCase,
            UpdateOrderItemProductSpecificationUseCase updateOrderItemProductSpecificationUseCase,
            OrderPresentationMapper orderPresentationMapper
    ) {
        this.createOrderFromQuotationUseCase = createOrderFromQuotationUseCase;
        this.getOrdersUseCase = getOrdersUseCase;
        this.getOrderUseCase = getOrderUseCase;
        this.getOrderProfitabilityUseCase = getOrderProfitabilityUseCase;
        this.getOrderProfitabilityListUseCase = getOrderProfitabilityListUseCase;
        this.replaceOrderItemSizesUseCase = replaceOrderItemSizesUseCase;
        this.updateOrderItemProductSpecificationUseCase = updateOrderItemProductSpecificationUseCase;
        this.orderPresentationMapper = orderPresentationMapper;
    }

    @GetMapping
    public ResponseEntity<GetOrdersResponse> getOrders() {
        GetOrdersResult result = getOrdersUseCase.execute();
        GetOrdersResponse response = orderPresentationMapper.toResponse(result);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profitability")
    public ResponseEntity<GetOrderProfitabilityListResponse> getOrderProfitabilityList() {
        GetOrderProfitabilityListResult result = getOrderProfitabilityListUseCase.execute();
        return ResponseEntity.ok(orderPresentationMapper.toResponse(result));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<GetOrderResponse> getOrder(
            @PathVariable UUID orderId
    ) {
        GetOrderCommand command = orderPresentationMapper.toGetOrderCommand(orderId);
        GetOrderResult result = getOrderUseCase.execute(command);
        GetOrderResponse response = orderPresentationMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}/profitability")
    public ResponseEntity<GetOrderProfitabilityResponse> getOrderProfitability(
            @PathVariable UUID orderId
    ) {
        GetOrderProfitabilityQuery query = orderPresentationMapper.toGetOrderProfitabilityQuery(orderId);
        GetOrderProfitabilityResult result = getOrderProfitabilityUseCase.execute(query);
        GetOrderProfitabilityResponse response = orderPresentationMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(
            @RequestBody CreateOrderRequest request
    ) {
        CreateOrderFromQuotationCommand command = orderPresentationMapper.toCommand(request);
        CreateOrderFromQuotationResult result = createOrderFromQuotationUseCase.execute(command);
        CreateOrderResponse response = orderPresentationMapper.toResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{orderId}/items/{orderItemId}/sizes")
    public ResponseEntity<ReplaceOrderItemSizesResponse> replaceOrderItemSizes(
            @PathVariable UUID orderId,
            @PathVariable UUID orderItemId,
            @RequestBody ReplaceOrderItemSizesRequest request
    ) {
        ReplaceOrderItemSizesCommand command = orderPresentationMapper.toReplaceOrderItemSizesCommand(
                orderId,
                orderItemId,
                request
        );
        ReplaceOrderItemSizesResult result = replaceOrderItemSizesUseCase.execute(command);
        ReplaceOrderItemSizesResponse response = orderPresentationMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{orderId}/items/{orderItemId}/product-specification")
    public ResponseEntity<UpdateOrderItemProductSpecificationResponse> updateOrderItemProductSpecification(
            @PathVariable UUID orderId,
            @PathVariable UUID orderItemId,
            @RequestBody UpdateOrderItemProductSpecificationRequest request
    ) {
        UpdateOrderItemProductSpecificationCommand command =
                orderPresentationMapper.toUpdateOrderItemProductSpecificationCommand(
                        orderId,
                        orderItemId,
                        request
                );
        UpdateOrderItemProductSpecificationResult result =
                updateOrderItemProductSpecificationUseCase.execute(command);
        UpdateOrderItemProductSpecificationResponse response = orderPresentationMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }
}
