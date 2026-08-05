package com.magyen.platform.production.presentation.productionorder.controller;

import com.magyen.platform.production.application.dto.CreateProductionOrderCommand;
import com.magyen.platform.production.application.dto.CreateProductionOrderResult;
import com.magyen.platform.production.application.usecase.CreateProductionOrderFromOrderUseCase;
import com.magyen.platform.production.presentation.productionorder.mapper.ProductionPresentationMapper;
import com.magyen.platform.production.presentation.productionorder.request.CreateProductionOrderRequest;
import com.magyen.platform.production.presentation.productionorder.response.CreateProductionOrderResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone la API REST de órdenes de producción.
 * <p>
 * Coordina HTTP con Application; no contiene reglas de negocio.
 */
@RestController
@RequestMapping("/api/v1/production-orders")
public class ProductionOrderController {

    private final CreateProductionOrderFromOrderUseCase createProductionOrderFromOrderUseCase;
    private final ProductionPresentationMapper productionPresentationMapper;

    public ProductionOrderController(
            CreateProductionOrderFromOrderUseCase createProductionOrderFromOrderUseCase,
            ProductionPresentationMapper productionPresentationMapper
    ) {
        this.createProductionOrderFromOrderUseCase = createProductionOrderFromOrderUseCase;
        this.productionPresentationMapper = productionPresentationMapper;
    }

    @PostMapping
    public ResponseEntity<CreateProductionOrderResponse> createProductionOrder(
            @RequestBody CreateProductionOrderRequest request
    ) {
        CreateProductionOrderCommand command = productionPresentationMapper.toCommand(request);
        CreateProductionOrderResult result = createProductionOrderFromOrderUseCase.execute(command);
        CreateProductionOrderResponse response = productionPresentationMapper.toResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
