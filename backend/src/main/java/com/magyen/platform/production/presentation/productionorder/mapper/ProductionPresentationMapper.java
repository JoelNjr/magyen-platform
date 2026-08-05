package com.magyen.platform.production.presentation.productionorder.mapper;

import com.magyen.platform.production.application.dto.CreateProductionOrderCommand;
import com.magyen.platform.production.application.dto.CreateProductionOrderResult;
import com.magyen.platform.production.domain.ProductionPriority;
import com.magyen.platform.production.presentation.productionorder.request.CreateProductionOrderRequest;
import com.magyen.platform.production.presentation.productionorder.response.CreateProductionOrderResponse;

import java.util.Objects;

/**
 * Convierte entre objetos HTTP de Presentation y DTOs de Application.
 * <p>
 * No contiene reglas de negocio ni accede a repositorios, dominio o infraestructura.
 */
public class ProductionPresentationMapper {

    public CreateProductionOrderCommand toCommand(CreateProductionOrderRequest request) {
        Objects.requireNonNull(request, "CreateProductionOrderRequest must not be null");

        return new CreateProductionOrderCommand(
                request.orderId(),
                resolvePriority(request.priority()),
                request.plannedStartDate(),
                request.plannedEndDate(),
                request.observations()
        );
    }

    public CreateProductionOrderResponse toResponse(CreateProductionOrderResult result) {
        Objects.requireNonNull(result, "CreateProductionOrderResult must not be null");

        return new CreateProductionOrderResponse(
                result.productionOrderId(),
                result.orderId(),
                result.status().name(),
                result.priority().name(),
                result.creationDate()
        );
    }

    private ProductionPriority resolvePriority(String priority) {
        if (priority == null || priority.isBlank()) {
            return ProductionPriority.NORMAL;
        }

        return ProductionPriority.valueOf(priority);
    }
}
