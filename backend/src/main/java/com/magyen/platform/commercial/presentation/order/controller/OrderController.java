package com.magyen.platform.commercial.presentation.order.controller;

import com.magyen.platform.commercial.application.dto.AddOrderItemCommand;
import com.magyen.platform.commercial.application.dto.AddOrderItemResult;
import com.magyen.platform.commercial.application.dto.ApplyOrderDiscountCommand;
import com.magyen.platform.commercial.application.dto.ApplyOrderDiscountResult;
import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationResult;
import com.magyen.platform.commercial.application.dto.GetOrderCommand;
import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityListResult;
import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityQuery;
import com.magyen.platform.commercial.application.dto.GetOrderProfitabilityResult;
import com.magyen.platform.commercial.application.dto.GetOrderResult;
import com.magyen.platform.commercial.application.dto.GetOrdersQuery;
import com.magyen.platform.commercial.application.dto.GetOrdersResult;
import com.magyen.platform.commercial.application.dto.CommercialDocumentPdfResult;
import com.magyen.platform.commercial.application.dto.RemoveOrderItemCommand;
import com.magyen.platform.commercial.application.dto.RemoveOrderItemResult;
import com.magyen.platform.commercial.application.dto.ReplaceOrderItemSizesCommand;
import com.magyen.platform.commercial.application.dto.ReplaceOrderItemSizesResult;
import com.magyen.platform.commercial.application.dto.UpdateOrderItemCommand;
import com.magyen.platform.commercial.application.dto.UpdateOrderItemProductSpecificationCommand;
import com.magyen.platform.commercial.application.dto.UpdateOrderItemProductSpecificationResult;
import com.magyen.platform.commercial.application.dto.UpdateOrderItemResult;
import com.magyen.platform.commercial.application.usecase.AddOrderItemUseCase;
import com.magyen.platform.commercial.application.usecase.ApplyOrderDiscountUseCase;
import com.magyen.platform.commercial.application.usecase.CreateOrderFromQuotationUseCase;
import com.magyen.platform.commercial.application.usecase.GetOrderProfitabilityListUseCase;
import com.magyen.platform.commercial.application.usecase.GetOrderProfitabilityUseCase;
import com.magyen.platform.commercial.application.usecase.GetOrderUseCase;
import com.magyen.platform.commercial.application.usecase.GetOrdersUseCase;
import com.magyen.platform.commercial.application.usecase.GenerateOrderRemissionPdfUseCase;
import com.magyen.platform.commercial.application.usecase.RemoveOrderItemUseCase;
import com.magyen.platform.commercial.application.usecase.ReplaceOrderItemSizesUseCase;
import com.magyen.platform.commercial.application.usecase.UpdateOrderItemProductSpecificationUseCase;
import com.magyen.platform.commercial.application.usecase.UpdateOrderItemUseCase;
import com.magyen.platform.commercial.presentation.order.mapper.OrderPresentationMapper;
import com.magyen.platform.commercial.presentation.order.request.AddOrderItemRequest;
import com.magyen.platform.commercial.presentation.order.request.ApplyOrderDiscountRequest;
import com.magyen.platform.commercial.presentation.order.request.CreateOrderRequest;
import com.magyen.platform.commercial.presentation.order.request.ReplaceOrderItemSizesRequest;
import com.magyen.platform.commercial.presentation.order.request.UpdateOrderItemProductSpecificationRequest;
import com.magyen.platform.commercial.presentation.order.request.UpdateOrderItemRequest;
import com.magyen.platform.commercial.presentation.order.response.AddOrderItemResponse;
import com.magyen.platform.commercial.presentation.order.response.ApplyOrderDiscountResponse;
import com.magyen.platform.commercial.presentation.order.response.CreateOrderResponse;
import com.magyen.platform.commercial.presentation.order.response.GetOrderProfitabilityListResponse;
import com.magyen.platform.commercial.presentation.order.response.GetOrderProfitabilityResponse;
import com.magyen.platform.commercial.presentation.order.response.GetOrderResponse;
import com.magyen.platform.commercial.presentation.order.response.GetOrdersResponse;
import com.magyen.platform.commercial.presentation.order.response.RemoveOrderItemResponse;
import com.magyen.platform.commercial.presentation.order.response.ReplaceOrderItemSizesResponse;
import com.magyen.platform.commercial.presentation.order.response.UpdateOrderItemProductSpecificationResponse;
import com.magyen.platform.commercial.presentation.order.response.UpdateOrderItemResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
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
    private final GenerateOrderRemissionPdfUseCase generateOrderRemissionPdfUseCase;
    private final GetOrderProfitabilityUseCase getOrderProfitabilityUseCase;
    private final GetOrderProfitabilityListUseCase getOrderProfitabilityListUseCase;
    private final ReplaceOrderItemSizesUseCase replaceOrderItemSizesUseCase;
    private final UpdateOrderItemProductSpecificationUseCase updateOrderItemProductSpecificationUseCase;
    private final AddOrderItemUseCase addOrderItemUseCase;
    private final UpdateOrderItemUseCase updateOrderItemUseCase;
    private final RemoveOrderItemUseCase removeOrderItemUseCase;
    private final ApplyOrderDiscountUseCase applyOrderDiscountUseCase;
    private final OrderPresentationMapper orderPresentationMapper;

    public OrderController(
            CreateOrderFromQuotationUseCase createOrderFromQuotationUseCase,
            GetOrdersUseCase getOrdersUseCase,
            GetOrderUseCase getOrderUseCase,
            GenerateOrderRemissionPdfUseCase generateOrderRemissionPdfUseCase,
            GetOrderProfitabilityUseCase getOrderProfitabilityUseCase,
            GetOrderProfitabilityListUseCase getOrderProfitabilityListUseCase,
            ReplaceOrderItemSizesUseCase replaceOrderItemSizesUseCase,
            UpdateOrderItemProductSpecificationUseCase updateOrderItemProductSpecificationUseCase,
            AddOrderItemUseCase addOrderItemUseCase,
            UpdateOrderItemUseCase updateOrderItemUseCase,
            RemoveOrderItemUseCase removeOrderItemUseCase,
            ApplyOrderDiscountUseCase applyOrderDiscountUseCase,
            OrderPresentationMapper orderPresentationMapper
    ) {
        this.createOrderFromQuotationUseCase = createOrderFromQuotationUseCase;
        this.getOrdersUseCase = getOrdersUseCase;
        this.getOrderUseCase = getOrderUseCase;
        this.generateOrderRemissionPdfUseCase = generateOrderRemissionPdfUseCase;
        this.getOrderProfitabilityUseCase = getOrderProfitabilityUseCase;
        this.getOrderProfitabilityListUseCase = getOrderProfitabilityListUseCase;
        this.replaceOrderItemSizesUseCase = replaceOrderItemSizesUseCase;
        this.updateOrderItemProductSpecificationUseCase = updateOrderItemProductSpecificationUseCase;
        this.addOrderItemUseCase = addOrderItemUseCase;
        this.updateOrderItemUseCase = updateOrderItemUseCase;
        this.removeOrderItemUseCase = removeOrderItemUseCase;
        this.applyOrderDiscountUseCase = applyOrderDiscountUseCase;
        this.orderPresentationMapper = orderPresentationMapper;
    }

    @GetMapping
    public ResponseEntity<GetOrdersResponse> getOrders(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        GetOrdersResult result = getOrdersUseCase.execute(new GetOrdersQuery(fromDate, toDate));
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

    @GetMapping(value = "/{orderId}/remission/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getOrderRemissionPdf(@PathVariable UUID orderId) {
        GetOrderCommand command = orderPresentationMapper.toGetOrderCommand(orderId);
        CommercialDocumentPdfResult result = generateOrderRemissionPdfUseCase.execute(command);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition(result.filename()))
                .body(result.content());
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

    @PostMapping("/{orderId}/items")
    public ResponseEntity<AddOrderItemResponse> addOrderItem(
            @PathVariable UUID orderId,
            @RequestBody AddOrderItemRequest request
    ) {
        AddOrderItemCommand command = orderPresentationMapper.toAddOrderItemCommand(orderId, request);
        AddOrderItemResult result = addOrderItemUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderPresentationMapper.toResponse(result));
    }

    @PutMapping("/{orderId}/items/{orderItemId}")
    public ResponseEntity<UpdateOrderItemResponse> updateOrderItem(
            @PathVariable UUID orderId,
            @PathVariable UUID orderItemId,
            @RequestBody UpdateOrderItemRequest request
    ) {
        UpdateOrderItemCommand command = orderPresentationMapper.toUpdateOrderItemCommand(
                orderId,
                orderItemId,
                request
        );
        UpdateOrderItemResult result = updateOrderItemUseCase.execute(command);
        return ResponseEntity.ok(orderPresentationMapper.toResponse(result));
    }

    @DeleteMapping("/{orderId}/items/{orderItemId}")
    public ResponseEntity<RemoveOrderItemResponse> removeOrderItem(
            @PathVariable UUID orderId,
            @PathVariable UUID orderItemId
    ) {
        RemoveOrderItemCommand command = orderPresentationMapper.toRemoveOrderItemCommand(orderId, orderItemId);
        RemoveOrderItemResult result = removeOrderItemUseCase.execute(command);
        return ResponseEntity.ok(orderPresentationMapper.toResponse(result));
    }

    @PatchMapping("/{orderId}/discount")
    public ResponseEntity<ApplyOrderDiscountResponse> applyOrderDiscount(
            @PathVariable UUID orderId,
            @RequestBody ApplyOrderDiscountRequest request
    ) {
        ApplyOrderDiscountCommand command = orderPresentationMapper.toApplyOrderDiscountCommand(orderId, request);
        ApplyOrderDiscountResult result = applyOrderDiscountUseCase.execute(command);
        return ResponseEntity.ok(orderPresentationMapper.toResponse(result));
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

    private static String contentDisposition(String filename) {
        return "attachment; filename=\"" + filename + "\"";
    }
}
