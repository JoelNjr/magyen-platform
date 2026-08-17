package com.magyen.platform.production.presentation.operator.controller;

import com.magyen.platform.production.application.dto.CreateProductionOperatorCommand;
import com.magyen.platform.production.application.dto.CreateProductionOperatorResult;
import com.magyen.platform.production.application.dto.GetProductionOperatorsResult;
import com.magyen.platform.production.application.usecase.CreateProductionOperatorUseCase;
import com.magyen.platform.production.application.usecase.GetProductionOperatorsUseCase;
import com.magyen.platform.production.presentation.operator.mapper.ProductionOperatorPresentationMapper;
import com.magyen.platform.production.presentation.operator.request.CreateProductionOperatorRequest;
import com.magyen.platform.production.presentation.operator.response.CreateProductionOperatorResponse;
import com.magyen.platform.production.presentation.operator.response.GetProductionOperatorsResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone la API REST de operarios de producción.
 * <p>
 * Coordina HTTP con Application; no contiene reglas de negocio.
 */
@RestController
@RequestMapping("/api/v1/production/operators")
public class ProductionOperatorController {

    private final CreateProductionOperatorUseCase createProductionOperatorUseCase;
    private final GetProductionOperatorsUseCase getProductionOperatorsUseCase;
    private final ProductionOperatorPresentationMapper productionOperatorPresentationMapper;

    public ProductionOperatorController(
            CreateProductionOperatorUseCase createProductionOperatorUseCase,
            GetProductionOperatorsUseCase getProductionOperatorsUseCase,
            ProductionOperatorPresentationMapper productionOperatorPresentationMapper
    ) {
        this.createProductionOperatorUseCase = createProductionOperatorUseCase;
        this.getProductionOperatorsUseCase = getProductionOperatorsUseCase;
        this.productionOperatorPresentationMapper = productionOperatorPresentationMapper;
    }

    @GetMapping
    public ResponseEntity<GetProductionOperatorsResponse> getOperators() {
        GetProductionOperatorsResult result = getProductionOperatorsUseCase.execute();
        GetProductionOperatorsResponse response = productionOperatorPresentationMapper.toResponse(result);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CreateProductionOperatorResponse> createOperator(
            @RequestBody CreateProductionOperatorRequest request
    ) {
        CreateProductionOperatorCommand command = productionOperatorPresentationMapper.toCommand(request);
        CreateProductionOperatorResult result = createProductionOperatorUseCase.execute(command);
        CreateProductionOperatorResponse response = productionOperatorPresentationMapper.toResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
