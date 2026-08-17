package com.magyen.platform.production.presentation.operator.mapper;

import com.magyen.platform.production.application.dto.CreateProductionOperatorCommand;
import com.magyen.platform.production.application.dto.CreateProductionOperatorResult;
import com.magyen.platform.production.application.dto.GetProductionOperatorsResult;
import com.magyen.platform.production.application.dto.ProductionOperatorResult;
import com.magyen.platform.production.presentation.operator.request.CreateProductionOperatorRequest;
import com.magyen.platform.production.presentation.operator.response.CreateProductionOperatorResponse;
import com.magyen.platform.production.presentation.operator.response.GetProductionOperatorsResponse;
import com.magyen.platform.production.presentation.operator.response.ProductionOperatorResponse;

import java.util.List;
import java.util.Objects;

/**
 * Convierte entre objetos HTTP de Presentation y DTOs de Application para operarios.
 * <p>
 * No contiene reglas de negocio ni accede a repositorios, dominio o infraestructura.
 */
public class ProductionOperatorPresentationMapper {

    public CreateProductionOperatorCommand toCommand(CreateProductionOperatorRequest request) {
        Objects.requireNonNull(request, "CreateProductionOperatorRequest must not be null");

        return new CreateProductionOperatorCommand(request.name());
    }

    public CreateProductionOperatorResponse toResponse(CreateProductionOperatorResult result) {
        Objects.requireNonNull(result, "CreateProductionOperatorResult must not be null");

        return new CreateProductionOperatorResponse(
                result.operatorId(),
                result.name(),
                result.active()
        );
    }

    public GetProductionOperatorsResponse toResponse(GetProductionOperatorsResult result) {
        Objects.requireNonNull(result, "GetProductionOperatorsResult must not be null");

        List<ProductionOperatorResponse> operators = result.operators().stream()
                .map(this::toOperatorResponse)
                .toList();

        return new GetProductionOperatorsResponse(operators);
    }

    private ProductionOperatorResponse toOperatorResponse(ProductionOperatorResult operator) {
        return new ProductionOperatorResponse(
                operator.operatorId(),
                operator.name(),
                operator.active()
        );
    }
}
