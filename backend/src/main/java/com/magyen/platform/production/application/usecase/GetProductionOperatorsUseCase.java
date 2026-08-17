package com.magyen.platform.production.application.usecase;

import com.magyen.platform.production.application.dto.GetProductionOperatorsResult;
import com.magyen.platform.production.application.dto.ProductionOperatorResult;
import com.magyen.platform.production.domain.ProductionOperator;
import com.magyen.platform.production.domain.ProductionOperatorRepository;

import java.util.List;
import java.util.Objects;

/**
 * Caso de uso que consulta los operarios de producción existentes.
 */
public class GetProductionOperatorsUseCase {

    private final ProductionOperatorRepository productionOperatorRepository;

    public GetProductionOperatorsUseCase(ProductionOperatorRepository productionOperatorRepository) {
        this.productionOperatorRepository = Objects.requireNonNull(
                productionOperatorRepository,
                "Production operator repository must not be null"
        );
    }

    public GetProductionOperatorsResult execute() {
        List<ProductionOperatorResult> operators = productionOperatorRepository.findAll().stream()
                .map(this::toOperatorResult)
                .toList();

        return new GetProductionOperatorsResult(operators);
    }

    private ProductionOperatorResult toOperatorResult(ProductionOperator operator) {
        return new ProductionOperatorResult(
                operator.getId(),
                operator.getName(),
                operator.isActive()
        );
    }
}
