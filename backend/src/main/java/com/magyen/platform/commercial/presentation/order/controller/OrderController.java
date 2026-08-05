package com.magyen.platform.commercial.presentation.order.controller;

import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationCommand;
import com.magyen.platform.commercial.application.dto.CreateOrderFromQuotationResult;
import com.magyen.platform.commercial.application.usecase.CreateOrderFromQuotationUseCase;
import com.magyen.platform.commercial.presentation.order.mapper.OrderPresentationMapper;
import com.magyen.platform.commercial.presentation.order.request.CreateOrderRequest;
import com.magyen.platform.commercial.presentation.order.response.CreateOrderResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone la API REST de órdenes.
 * <p>
 * Coordina HTTP con Application; no contiene reglas de negocio.
 */
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final CreateOrderFromQuotationUseCase createOrderFromQuotationUseCase;
    private final OrderPresentationMapper orderPresentationMapper;

    public OrderController(
            CreateOrderFromQuotationUseCase createOrderFromQuotationUseCase,
            OrderPresentationMapper orderPresentationMapper
    ) {
        this.createOrderFromQuotationUseCase = createOrderFromQuotationUseCase;
        this.orderPresentationMapper = orderPresentationMapper;
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
}
