package com.magyen.platform.production.presentation.labor.controller;

import com.magyen.platform.production.application.port.ProductionLaborOperatorInfo;
import com.magyen.platform.production.application.usecase.ListEligibleProductionLaborOperatorsUseCase;
import com.magyen.platform.production.presentation.labor.response.GetEligibleProductionLaborOperatorsResponse;
import com.magyen.platform.production.presentation.labor.response.ProductionLaborOperatorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Expone el selector de operarios elegibles para mano de obra por producción.
 */
@RestController
@RequestMapping("/api/v1/production/labor-operators")
public class ProductionLaborOperatorController {

    private final ListEligibleProductionLaborOperatorsUseCase listEligibleProductionLaborOperatorsUseCase;

    public ProductionLaborOperatorController(
            ListEligibleProductionLaborOperatorsUseCase listEligibleProductionLaborOperatorsUseCase
    ) {
        this.listEligibleProductionLaborOperatorsUseCase = listEligibleProductionLaborOperatorsUseCase;
    }

    @GetMapping
    public ResponseEntity<GetEligibleProductionLaborOperatorsResponse> listEligibleOperators() {
        List<ProductionLaborOperatorInfo> operators = listEligibleProductionLaborOperatorsUseCase.execute();
        return ResponseEntity.ok(new GetEligibleProductionLaborOperatorsResponse(
                operators.stream()
                        .map(operator -> new ProductionLaborOperatorResponse(
                                operator.employeeId(),
                                operator.displayName()
                        ))
                        .toList()
        ));
    }
}
